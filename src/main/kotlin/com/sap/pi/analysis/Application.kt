package com.sap.pi.analysis

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.sap.pi.analysis.health.healthAndMetrics
import com.sap.pi.analysis.exception.configureExceptionMapping

import com.sap.pi.analysis.rds.DataSourceConfig
import com.sap.pi.analysis.util.*
import com.sap.pi.analysis.route.configureRoutes
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import java.util.*
import javax.sql.DataSource

lateinit var applicationConfig: ApplicationConfig
lateinit var dataSource: DataSource

fun main(args: Array<String>) {
    val applicationEnvironment = commandLineEnvironment(args)
    if (applicationEnvironment.config.property("ktor.profile").getString() == "migration") {
        // migration mode - just run flyway migration and then exit, don't even start ktor
        migrate(applicationEnvironment.config)
        return
    }

    val env = applicationEngineEnvironment {
        module {
            when (applicationEnvironment.config.property("ktor.profile").getString()) {
                "development" -> {
                    main(applicationEnvironment)                    
                }

                "deployed" -> {
                    main(applicationEnvironment)                    
                    healthAndMetrics()
                }
                else -> throw RuntimeException("Unknown ktor.profile value")
            }
        }
        // admin port
        connector {
            port = applicationEnvironment.config.property("ktor.deployment.adminPort").getString().toInt()
        }
        // public port
        connector {
            port = applicationEnvironment.config.property("ktor.deployment.port").getString().toInt()
        }
    }
    embeddedServer(Netty, env).start(true)
}

fun Application.main(environment: ApplicationEnvironment) {
    applicationConfig = environment.config
    
    if (!::dataSource.isInitialized) {
        dataSource = DataSourceConfig.createConnectionPool(applicationConfig)
    }
    
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            registerModules(
                kotlinModule(),
                JavaTimeModule(),
                SimpleModule()
                    .addSerializer(UUID::class.java, UUIDWithoutDashesSerializer())
                    .addDeserializer(UUID::class.java, DashTolerantUUIDDeserializer()),
            )
        }
    }

    install(Authentication) {
        jwt {
            val token = JWT.require(Algorithm.HMAC256(applicationConfig.property("jwt.signingKey").getString())).build()
            verifier(
                token,
            )
            validate { credential -> JWTPrincipal(credential.payload) }

            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }

    install(CallLogging) {
        filter { call -> !call.request.path().startsWith("/health") }

        mdc("sgv_tenant") {
            getClaimFromToken(it.request.header("authorization"), "ten")
        }
        mdc("sgv_request") {
            getClaimFromToken(it.request.header("authorization"), "rid")
        }
        mdc("sgv_user") {
            removeDashesFromStringUuid(getClaimFromToken(it.request.header("authorization"), "uid")!!)
        }
    }

    healthAndMetrics()
    configureExceptionMapping()
    configureRoutes()
}

package com.sap.pi.analysis.health

import com.codahale.metrics.health.HealthCheckRegistry
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.logging.LogbackMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry


object HealthCheckService {
    val registry: HealthCheckRegistry = HealthCheckRegistry()
}

fun Application.healthAndMetrics() {
    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
        meterBinders = listOf(
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            ProcessorMetrics(),
            LogbackMetrics(),
            ClassLoaderMetrics(),
            JvmThreadMetrics(),
        )
    }
    routing {
        get("/health") {
            val results = HealthCheckService.registry.runHealthChecks()
            val checkResult = results.flatMap { (key, values) -> listOf(key).plus(values) }.toString()
            call.respondText(checkResult, ContentType.Application.Json)
        }
        get("/metrics") {
            if (isAdminPort(call)) {
                call.respond(appMicrometerRegistry.scrape())
            }
        }
    }
}

private fun isAdminPort(call: ApplicationCall): Boolean {
    val adminPort = 8090
    return call.request.local.port == adminPort
}

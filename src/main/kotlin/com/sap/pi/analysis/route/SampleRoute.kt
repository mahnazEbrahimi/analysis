package com.sap.pi.analysis.route

import com.sap.pi.analysis.dataSource
import com.sap.pi.analysis.api.getUserInformation
import com.sap.pi.analysis.util.RequestContext
import com.sap.pi.analysis.rds.DataSourceConfig
import com.sap.pi.analysis.rds.startSession
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import org.jooq.DSLContext
import org.jooq.kotlin.coroutines.transactionCoroutine
import kotlin.reflect.KSuspendFunction2

fun Application.configureRoutes() {
    routing {
        route("/analysis") {
            authenticate {
                get { 
                    runInRequestScope(this::getUser)                
                }
            }
        }
    }
}

internal suspend fun PipelineContext<Unit, ApplicationCall>.runInRequestScope(handler: KSuspendFunction2<DSLContext, RequestContext, Unit>) {
    val dslContext = DataSourceConfig.createDSLContext(dataSource)
    val userInfo = getUserInformation(call.principal()!!)
    val authToken = call.request.header("authorization")!!.removePrefix("Bearer ")
    val requestContext = RequestContext(userInfo, authToken)
    dslContext.transactionCoroutine { trx ->
        trx.dsl().startSession(userInfo)
        handler(trx.dsl(), requestContext)
    }
}

context(DSLContext, RequestContext)
private suspend fun PipelineContext<Unit, ApplicationCall>.getUser() {
    call.respondText("Hello World ${user.userId}!")
}
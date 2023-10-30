package com.sap.pi.analysis.route

import com.sap.pi.analysis.api.getUserInformation
import com.sap.pi.analysis.util.RequestContext
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRoutes() {
    routing {
        route("/analysis") {
            authenticate {
                get { 
                    val userInfo = getUserInformation(call.principal()!!)
                    val authToken = call.request.header("authorization")!!
                    val requestContext = RequestContext(userInfo, authToken)
                    call.respondText("Hello World ${requestContext.user}!")
                }
            }
        }
    }
}
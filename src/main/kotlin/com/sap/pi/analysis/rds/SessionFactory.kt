package com.sap.pi.analysis.rds

import com.sap.pi.analysis.applicationConfig
import com.sap.pi.analysis.api.UserInformation
import com.sap.pi.analysis.generated.routines.SetSessionVariablesV2
import org.jooq.DSLContext

fun DSLContext.startSession(jwtPrincipal: UserInformation) {
    val sessionVariables = SetSessionVariablesV2()
    with(sessionVariables) {
        setPTenantId(jwtPrincipal.tenantId)
        setPUserId(jwtPrincipal.userId)
        setPPassphrase(applicationConfig.propertyOrNull("datasource.password")?.getString())
    }
    sessionVariables.execute(this.configuration())
}

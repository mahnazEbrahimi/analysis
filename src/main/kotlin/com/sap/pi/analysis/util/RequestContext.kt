package com.sap.pi.analysis.util

import com.sap.pi.analysis.api.UserInformation

data class RequestContext(
    val user: UserInformation,
    val authToken: String,
)

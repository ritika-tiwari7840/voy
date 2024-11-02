package com.ritika.voy.api.dataclasses


data class SignUpResponse(
    val success: Boolean?,
    val message: String?,
    val token: String?,
    val error:String?
)
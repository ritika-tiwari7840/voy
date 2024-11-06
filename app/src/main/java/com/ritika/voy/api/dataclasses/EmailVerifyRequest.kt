package com.ritika.voy.api.dataclasses

data class EmailVerifyRequest(
    val user_id: String?,
    val email_otp: String,
)

package com.ritika.voy.api.dataclasses

data class SignUpVerifyRequest(
    val user_id: String?,
    val email_otp: String,
    val phone_otp: String
)

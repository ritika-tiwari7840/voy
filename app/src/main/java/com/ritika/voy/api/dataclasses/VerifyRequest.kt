package com.ritika.voy.api.dataclasses

data class VerifyRequest(
    val email_otp: String,
    val phone_otp: String,
    val temp_user_id: String
)
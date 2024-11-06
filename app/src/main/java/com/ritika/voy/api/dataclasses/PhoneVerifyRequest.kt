package com.ritika.voy.api.dataclasses

data class PhoneVerifyRequest(
    val phone_otp: String,
    val user_id: String
)
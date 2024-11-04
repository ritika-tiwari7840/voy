package com.ritika.voy.api.dataclasses

data class VerifyRequest(
    val email: String,
    val otp: String
)
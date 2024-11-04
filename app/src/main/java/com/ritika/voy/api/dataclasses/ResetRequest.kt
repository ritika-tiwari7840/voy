package com.ritika.voy.api.dataclasses

data class ResetRequest(
    val email: String,
    val otp: String,
    val new_password: String,
    val confirm_password: String
)
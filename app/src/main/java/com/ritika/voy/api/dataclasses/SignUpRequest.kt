package com.ritika.voy.api.dataclasses

data class SignUpRequest(
    val email: String,
    val password: String,
    val confirmPassword: String,
    val phone: String
)


package com.ritika.voy.api.dataclasses

data class SignUpRequest(
    val confirm_password: String,
    val email: String,
    val password: String,
    val phone_number: String
)
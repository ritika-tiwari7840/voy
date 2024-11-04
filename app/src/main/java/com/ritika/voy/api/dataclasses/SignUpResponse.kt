package com.ritika.voy.api.dataclasses

data class SignUpResponse(
    val message: String,
    val success: Boolean,
    val user_id: Int? = null,
    val errors: Errors? = null
)

data class Errors(
    val email: List<String>? = null,
    val phone_number: List<String>? = null
)
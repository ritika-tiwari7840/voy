package com.ritika.voy.api.dataclasses

data class SignUpErrorResponse(
    val success: Boolean,
    val message: String,
    val registration_status: RegistrationStatus?,
    val errors: Map<String, List<String>>
)


package com.ritika.voy.api.dataclasses

data class SignUpResponse(
    val message: String,
    val registration_status: RegistrationStatus,
    val success: Boolean
)
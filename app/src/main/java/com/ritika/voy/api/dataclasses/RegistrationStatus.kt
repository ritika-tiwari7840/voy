package com.ritika.voy.api.dataclasses

data class RegistrationStatus(
    val email: String,
    val expires_in: ExpiresIn,
    val next_step: String,
    val phone_number: String,
    val user_id: Int,
    val verification_status: VerificationStatus,
    val email_verified: String,
    val phone_verified: String,
)
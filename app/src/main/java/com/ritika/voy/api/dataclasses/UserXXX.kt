package com.ritika.voy.api.dataclasses

data class UserXXX(
    val created_at: String,
    val drivers_license_image: Any,
    val email: String,
    val email_verified: Boolean,
    val emergency_contact_phone: String,
    val first_name: String,
    val full_name: String,
    val gender: String,
    val id: Int,
    val is_driver_verified: Boolean,
    val last_name: String,
    val phone_number: String,
    val phone_verified: Boolean,
    val profile_photo: Any
)
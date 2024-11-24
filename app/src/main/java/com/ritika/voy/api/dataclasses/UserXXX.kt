package com.ritika.voy.api.dataclasses

data class UserXXX(
    val completed_rides_as_driver: Int,
    val completed_rides_as_passenger: Int,
    val created_at: String,
    val drivers_license_image: String,
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
    val profile_photo: Any,
    val rating_as_driver: Double,
    val rating_as_passenger: Double,
    val vehicle_model: Any,
    val vehicle_number: Any
)
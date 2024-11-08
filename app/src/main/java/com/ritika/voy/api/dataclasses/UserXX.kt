package com.ritika.voy.api.dataclasses

data class UserXX(
    val id: Int,
    val email: String,
    val phone_number: String,
    val first_name: String?,
    val last_name: String?,
    val full_name: String?,
    val created_at: String,
    val email_verified: Boolean,
    val phone_verified: Boolean
)
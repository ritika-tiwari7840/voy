package com.ritika.voy.api.dataclasses

data class UserX(
    val created_at: String,
    val email: String,
    val email_verified: Boolean,
    val first_name: String,
    val full_name: String,
    val id: Int,
    val last_name: String,
    val phone_number: String,
    val phone_verified: Boolean
)
package com.ritika.voy.api.dataclasses

data class UserDataResponse(
    val success: Boolean,
    val message: String,
    val data: UserData
)

data class UserData(
    val id: Int,
    val first_name: String,
    val last_name: String,
    val gender: String,
    val profile_photo: String,
    val emergency_contact_phone: String
)

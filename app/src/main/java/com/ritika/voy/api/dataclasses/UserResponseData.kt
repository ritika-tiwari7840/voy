package com.ritika.voy.api.dataclasses

data class UserResponseData(
    val message: String,
    val success: Boolean,
    val user: UserXXX
)
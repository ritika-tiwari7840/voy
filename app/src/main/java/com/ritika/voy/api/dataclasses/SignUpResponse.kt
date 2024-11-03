package com.ritika.voy.api.dataclasses

data class SignUpResponse(
    val message: String,
    val status: String,
    val success: Boolean,
    val temp_user_id: String
)
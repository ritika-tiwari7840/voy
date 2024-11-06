package com.ritika.voy.api.dataclasses

data class PhoneVerifyResponse(
    val message: String,
    val success: Boolean,
    val tokens: Tokens,
    val user: User
)
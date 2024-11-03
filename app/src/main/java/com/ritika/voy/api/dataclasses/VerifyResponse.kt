package com.ritika.voy.api.dataclasses

data class VerifyResponse(
    val message: String,
    val status: String,
    val success: Boolean,
    val tokens: Tokens,
    val user: User
)
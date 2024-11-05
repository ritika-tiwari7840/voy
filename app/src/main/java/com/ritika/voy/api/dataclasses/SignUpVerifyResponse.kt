package com.ritika.voy.api.dataclasses

data class SignUpVerifyResponse(
    val message: String,
    val success: Boolean,
    val tokens: TokensX,
    val user: UserX
)
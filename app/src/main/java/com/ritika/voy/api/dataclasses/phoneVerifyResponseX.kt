package com.ritika.voy.api.dataclasses

data class phoneVerifyResponseX(
    val message: String,
    val success: Boolean,
    val tokens: TokensX,
    val user: UserX,
    val errors: Map<String, List<String>>,
)
package com.ritika.voy.api.dataclasses

data class EmailVerifyResponse(
    val success: Boolean,
    val message: String,
    val user_id: String,
    val errors: Map<String, List<String>>,
)
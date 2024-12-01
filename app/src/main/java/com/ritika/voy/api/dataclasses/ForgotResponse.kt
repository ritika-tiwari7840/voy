package com.ritika.voy.api.dataclasses

data class ForgotResponse(
    val message: String,
    val success: Boolean,
    val errors: Map<String, List<String>>
)
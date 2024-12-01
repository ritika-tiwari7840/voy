package com.ritika.voy.api.dataclasses

data class ResetResponse(
    val message: String,
    val success: Boolean,
    val errors: Map<String,List<String>>,
)
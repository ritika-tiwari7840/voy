package com.ritika.voy.api.dataclasses

import androidx.core.app.NotificationCompat.MessagingStyle.Message

data class RideRequestResponse(
    val data: DataXX?,
    val success: Boolean,
    val message: String,
)
data class ErrorResponse(
    val non_field_errors: List<String>?
)
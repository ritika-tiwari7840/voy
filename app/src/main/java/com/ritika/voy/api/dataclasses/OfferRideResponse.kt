package com.ritika.voy.api.dataclasses

import androidx.core.app.NotificationCompat.MessagingStyle.Message

data class OfferRideResponse(
    val data: DataXXX?,
    val success: Boolean,
    val message: String,
    val error: String,
)
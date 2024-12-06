package com.ritika.voy.api.dataclasses

data class DriverManageRequestResponse(
    val success: Boolean,
    val data: RideData,
)

data class RideData(
    val message: String,
    val available_seats: Int,
)

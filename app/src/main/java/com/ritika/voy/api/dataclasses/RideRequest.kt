package com.ritika.voy.api.dataclasses

data class RideRequest(
    val pickup_location: String,
    val dropoff_location: String,
    val pickup_point: LocationPoint,
    val dropoff_point: LocationPoint,
    val seats_needed: Int
)
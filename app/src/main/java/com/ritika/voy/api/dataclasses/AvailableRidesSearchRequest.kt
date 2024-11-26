package com.ritika.voy.api.dataclasses

data class AvailableRidesSearchRequest(
    val pickup_point: LocationPoint,
    val destination_point: LocationPoint,
    val seats_needed: Int,
    val radius: Double
)

data class LocationPoint(
    val type: String,
    val coordinates: List<Double>
)

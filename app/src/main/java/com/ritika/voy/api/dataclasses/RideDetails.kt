package com.ritika.voy.api.dataclasses

data class RideDetails(
    val id: Int,
    val driver_name: String,
    val start_location: String,
    val end_location: String,
    val start_time: String,
    val status: String,
    val available_seats: Int,
    val passenger_requests: List<PassengerRequest>
)
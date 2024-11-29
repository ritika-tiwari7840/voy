package com.ritika.voy.api.dataclasses

data class PassengerRequestData(
    val id: Int,
    val passenger_name: String,
    val pickup_location: String,
    val dropoff_location: String,
    val seats_needed: Int,
    val status: String,
    val payment_completed: Boolean
)
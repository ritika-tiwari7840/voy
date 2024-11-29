package com.ritika.voy.api.dataclasses

data class DataXX(
    val created_at: String,
    val dropoff_location: String,
    val dropoff_point: DropoffPoint,
    val id: Int,
    val passenger: Int,
    val passenger_id: Int,
    val passenger_name: String,
    val payment_completed: Boolean,
    val pickup_location: String,
    val pickup_point: PickupPoint,
    val ride: Int,
    val seats_needed: Int,
    val status: String
)
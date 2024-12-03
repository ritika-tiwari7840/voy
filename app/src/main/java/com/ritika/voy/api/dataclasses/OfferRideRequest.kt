package com.ritika.voy.api.dataclasses

data class OfferRideRequest(
    val start_location: String,
    val end_location: String,
    val start_point: StartPoint,
    val end_point: EndPoint,
    val start_time: String,
    val available_seats: Int,
    )
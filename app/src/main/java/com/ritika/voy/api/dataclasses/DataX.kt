package com.ritika.voy.api.dataclasses

data class DataX(
    val available_seats: Int,
    val created_at: String,
    val driver: Int,
    val driver_name: String,
    val end_location: String,
    val end_point: EndPoint,
    val id: Int,
    val route_line: Any,
    val start_location: String,
    val start_point: StartPoint,
    val start_time: String,
    val status: String
)
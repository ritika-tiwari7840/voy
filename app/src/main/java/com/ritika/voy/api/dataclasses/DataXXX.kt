package com.ritika.voy.api.dataclasses

data class DataXXX(
    val available_seats: Int,
    val created_at: String,
    val driver: Int,
    val driver_name: String,
    val end_location: String,
    val end_point: EndPointXX,
    val id: Int,
    val route_line: Any,
    val start_location: String,
    val start_point: StartPointXX,
    val start_time: String,
    val status: String
)
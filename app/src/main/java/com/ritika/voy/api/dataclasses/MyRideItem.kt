package com.ritika.voy.api.dataclasses

import java.io.Serializable

data class MyRideItem(
    val driverName: String,
    val startLocation: String,
    val endLocation: String,
    val startTime: String,
    val status: String,
    val id: Int,
    val driver: Int
): Serializable

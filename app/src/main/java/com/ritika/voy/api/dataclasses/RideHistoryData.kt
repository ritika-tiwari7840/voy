package com.ritika.voy.api.dataclasses

data class RideHistoryData(
    val as_driver: List<Any>,
    val as_passenger: List<RideDetails>
)
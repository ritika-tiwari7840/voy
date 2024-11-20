package com.ritika.voy.api.dataclasses.mapsDataClasses

data class RoutesResponse(
    val routes: List<Route>
)

data class Route(
    val duration: String,
    val distanceMeters: Int,
    val polyline: Polyline
)


data class Polyline(
    val encodedPolyline: String
)

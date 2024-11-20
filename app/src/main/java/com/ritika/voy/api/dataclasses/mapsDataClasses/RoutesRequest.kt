package com.ritika.voy.api.dataclasses.mapsDataClasses

import com.google.android.gms.maps.model.LatLng

data class OriginDestination(
    val location: Location
)

data class Location(
    val latLng: LatLng
)

data class RouteModifiers(
    val avoidTolls: Boolean,
    val avoidHighways: Boolean,
    val avoidFerries: Boolean
)

data class RoutesRequest(
    val origin: OriginDestination,
    val destination: OriginDestination,
    val travelMode: String = "DRIVE",
    val routingPreference: String = "TRAFFIC_AWARE_OPTIMAL",
    val computeAlternativeRoutes: Boolean = false,
    val routeModifiers: RouteModifiers,
    val languageCode: String = "en-US",
    val units: String = "METRIC"
)
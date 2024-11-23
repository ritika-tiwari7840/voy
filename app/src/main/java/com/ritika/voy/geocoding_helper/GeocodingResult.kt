package com.ritika.voy.geocoding_helper

data class GeocodingResult(
    val latitude: Double,
    val longitude: Double,
    val formattedAddress: String? = null,
    val success: Boolean,
    val errorMessage: String? = null
)

package com.ritika.voy.geocoding_helper

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.pow

class GeocodingHelper(private val context: Context) {

    private val cache: LruCache<String, GeocodingResult> =
        LruCache(100) // Cache for geocoding results
    private val requestTimestamps = mutableListOf<Long>()
    private val rateLimitPeriod = TimeUnit.MINUTES.toMillis(1)
    private val maxRequestsPerMinute = 60

    /** Validates if the provided address string meets basic criteria */
    private fun validateAddress(address: String): Boolean {
        if (address.isBlank()) return false
        if (address.length < 3) return false
        val invalidChars = setOf('<', '>', '{', '}', '[', ']', '|', '\\')
        return !address.any { it in invalidChars }
    }

    /** Validates if the provided coordinates are within valid ranges */
    private fun validateCoordinates(latitude: Double, longitude: Double): Boolean {
        return latitude in -90.0..90.0 && longitude in -180.0..180.0
    }

    /** Checks if we're within rate limits */
    @Synchronized
    private fun checkRateLimit(): Boolean {
        val currentTime = System.currentTimeMillis()
        // Remove old timestamps
        requestTimestamps.removeAll { timestamp ->
            currentTime - timestamp > rateLimitPeriod
        }
        return requestTimestamps.size < maxRequestsPerMinute
    }

    /** Records a new request timestamp for rate limiting */
    @Synchronized
    private fun recordRequest() {
        requestTimestamps.add(System.currentTimeMillis())
    }

    /** Generates a cache key for coordinates */
    private fun generateCoordinatesCacheKey(latitude: Double, longitude: Double): String {
        // Round to 6 decimal places for cache key to handle minor floating point differences
        return "%.6f,%.6f".format(latitude, longitude)
    }

    /**
     * Geocodes an address to get its latitude and longitude. This function
     * should be called from a coroutine as it performs network operations.
     */
    suspend fun geocodeAddress(address: String): GeocodingResult = withContext(Dispatchers.IO) {
        try {
            // Check address validation
            if (!validateAddress(address)) {
                return@withContext GeocodingResult(
                    latitude = 0.0,
                    longitude = 0.0,
                    success = false,
                    errorMessage = "Invalid address format"
                )
            }

            // Check cache
            cache.get(address)?.let { return@withContext it }

            // Check rate limit
            if (!checkRateLimit()) {
                return@withContext GeocodingResult(
                    latitude = 0.0,
                    longitude = 0.0,
                    success = false,
                    errorMessage = "Rate limit exceeded. Please try again later."
                )
            }

            recordRequest()
            val geocoder = Geocoder(context, Locale.getDefault())

            // Use suspend function to geocode the address
            val addresses = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocationName(address, 1)
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(address, 1)
            }

            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val location = addresses[0]
                    val result = GeocodingResult(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        formattedAddress = formatAddress(location),
                        success = true
                    )
                    cache.put(address, result)
                    return@withContext result
                }
            }

            return@withContext GeocodingResult(
                latitude = 0.0,
                longitude = 0.0,
                success = false,
                errorMessage = "No results found for the address"
            )

        } catch (e: IOException) {
            return@withContext GeocodingResult(
                latitude = 0.0,
                longitude = 0.0,
                success = false,
                errorMessage = "Error geocoding address: ${e.localizedMessage}"
            )
        }
    }

    /** Reverse geocodes coordinates to get an address. */
    suspend fun reverseGeocode(latitude: Double, longitude: Double): GeocodingResult =
        withContext(Dispatchers.IO) {
            try {
                // Validate coordinates
                if (!validateCoordinates(latitude, longitude)) {
                    return@withContext GeocodingResult(
                        latitude = latitude,
                        longitude = longitude,
                        success = false,
                        errorMessage = "Invalid coordinates"
                    )
                }

                // Check cache
                val cacheKey = generateCoordinatesCacheKey(latitude, longitude)
                cache.get(cacheKey)?.let { return@withContext it }

                // Check rate limit
                if (!checkRateLimit()) {
                    return@withContext GeocodingResult(
                        latitude = latitude,
                        longitude = longitude,
                        success = false,
                        errorMessage = "Rate limit exceeded. Please try again later."
                    )
                }

                recordRequest()
                val geocoder = Geocoder(context, Locale.getDefault())

                // Use suspend function to reverse geocode the coordinates
                val addresses = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(latitude, longitude, 1)
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(latitude, longitude, 1)
                }

                if (addresses != null) {
                    if (addresses.isNotEmpty()) {
                        val location = addresses[0]
                        val result = GeocodingResult(
                            latitude = latitude,
                            longitude = longitude,
                            formattedAddress = formatAddress(location),
                            success = true
                        )
                        cache.put(cacheKey, result)
                        return@withContext result
                    }
                }

                return@withContext GeocodingResult(
                    latitude = latitude,
                    longitude = longitude,
                    success = false,
                    errorMessage = "No address found for these coordinates"
                )

            } catch (e: IOException) {
                return@withContext GeocodingResult(
                    latitude = latitude,
                    longitude = longitude,
                    success = false,
                    errorMessage = "Error reverse geocoding: ${e.localizedMessage}"
                )
            }
        }

    /** Formats an Address object into a readable string */
    private fun formatAddress(address: Address): String {
        val parts = mutableListOf<String>()

        // Add street address
        if (!address.thoroughfare.isNullOrBlank()) {
            val streetNumber = address.subThoroughfare ?: ""
            parts.add("$streetNumber ${address.thoroughfare}")
        }

        // Add city
        if (!address.locality.isNullOrBlank()) {
            parts.add(address.locality)
        }

        // Add state/province
        if (!address.adminArea.isNullOrBlank()) {
            parts.add(address.adminArea)
        }

        // Add postal code
        if (!address.postalCode.isNullOrBlank()) {
            parts.add(address.postalCode)
        }

        // Add country
        if (!address.countryName.isNullOrBlank()) {
            parts.add(address.countryName)
        }

        return parts.filter { it.isNotBlank() }.joinToString(", ")
    }

    /** Clears the geocoding cache */
    fun clearCache() {
        cache.evictAll()
    }

    /** Finds nearby addresses within a given radius */
    suspend fun findNearbyAddresses(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 1.0,
        maxResults: Int = 5,
    ): List<GeocodingResult> = withContext(Dispatchers.IO) {
        try {
            if (!validateCoordinates(latitude, longitude)) {
                return@withContext emptyList()
            }

            val geocoder = Geocoder(context, Locale.getDefault())
            val results = mutableListOf<GeocodingResult>()

            // Use suspend function to get nearby addresses
            val addresses = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(latitude, longitude, maxResults)
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, maxResults)
            }

            addresses?.forEach { address ->
                val distance = calculateDistance(
                    latitude, longitude,
                    address.latitude, address.longitude
                )

                if (distance <= radiusKm) {
                    results.add(
                        GeocodingResult(
                            latitude = address.latitude,
                            longitude = address.longitude,
                            formattedAddress = formatAddress(address),
                            success = true
                        )
                    )
                }
            }

            return@withContext results

        } catch (e: IOException) {
            return@withContext emptyList()
        }
    }

    /** Calculates distance between two points using the Haversine formula */
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double,
    ): Double {
        val R = 6371.0 // Earth's radius in kilometers

        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val latDiff = Math.toRadians(lat2 - lat1)
        val lonDiff = Math.toRadians(lon2 - lon1)

        val a = Math.sin(latDiff / 2).pow(2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                Math.sin(lonDiff / 2).pow(2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return R * c
    }
}

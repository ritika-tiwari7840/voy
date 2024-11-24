package com.ritika.voy.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import com.ritika.voy.BuildConfig
import com.ritika.voy.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder

class MapFragment: Fragment(), OnMapReadyCallback {

    private var _googleMap: GoogleMap? = null
    private val googleMap get() = _googleMap!!
    private val apiKey = BuildConfig.MAP_API_KEY
    private val client = OkHttpClient()

    private lateinit var startAddressInput: EditText
    private lateinit var endAddressInput: EditText
    private lateinit var navigateButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI components
        startAddressInput = view.findViewById(R.id.startAddressInput)
        endAddressInput = view.findViewById(R.id.endAddressInput)
        navigateButton = view.findViewById(R.id.navigateButton)

        // Set default addresses for testing
        startAddressInput.setText("123 Main St, New York, NY")
        endAddressInput.setText("Times Square, New York, NY")

        navigateButton.setOnClickListener {
            val startAddress = startAddressInput.text.toString()
            val endAddress = endAddressInput.text.toString()

            if (startAddress.isNotEmpty() && endAddress.isNotEmpty()) {
                geocodeAndNavigate(startAddress, endAddress)
            } else {
                showError("Please enter both addresses")
            }
        }

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        _googleMap = map
        googleMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
        }

        checkAndEnableLocation()
    }

    private fun geocodeAndNavigate(startAddress: String, endAddress: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val startLatLng = geocodeAddress(startAddress)
                val endLatLng = geocodeAddress(endAddress)

                if (startLatLng != null && endLatLng != null) {
                    // Clear previous markers and routes
                    googleMap.clear()

                    // Add markers and fetch route
                    addMarkersToMap(startLatLng, endLatLng)
                    fetchRouteAndDraw(startLatLng, endLatLng)

                    // Move camera to show both points
                    val bounds = com.google.android.gms.maps.model.LatLngBounds.Builder()
                        .include(startLatLng)
                        .include(endLatLng)
                        .build()
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                } else {
                    showError("Could not find one or both addresses")
                }
            } catch (e: Exception) {
                Log.e("MapFragment", "Error in geocoding: ${e.message}")
                showError("Error finding addresses")
            }
        }
    }

    private suspend fun geocodeAddress(address: String): LatLng? {
        return withContext(Dispatchers.IO) {
            try {
                val encodedAddress = URLEncoder.encode(address, "UTF-8")
                val url =
                    "https://maps.googleapis.com/maps/api/geocode/json?address=$encodedAddress&key=$apiKey"
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val results = jsonResponse.getJSONArray("results")
                    if (results.length() > 0) {
                        val location = results.getJSONObject(0)
                            .getJSONObject("geometry")
                            .getJSONObject("location")
                        val lat = location.getDouble("lat")
                        val lng = location.getDouble("lng")
                        return@withContext LatLng(lat, lng)
                    }
                }
                null
            } catch (e: Exception) {
                Log.e("MapFragment", "Geocoding error: ${e.message}")
                null
            }
        }
    }

    private fun checkAndEnableLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableMyLocation()
        } else {
            requestLocationPermission()
        }
    }

    private fun enableMyLocation() {
        try {
            _googleMap?.isMyLocationEnabled = true
        } catch (e: SecurityException) {
            Log.e("MapFragment", "Error enabling location: ${e.message}")
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun addMarkersToMap(origin: LatLng, destination: LatLng) {
        googleMap.apply {
            addMarker(
                MarkerOptions()
                    .position(origin)
                    .title("Start Location")
            )
            addMarker(
                MarkerOptions()
                    .position(destination)
                    .title("End Location")
            )
        }
    }

    private fun fetchRouteAndDraw(origin: LatLng, destination: LatLng) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val polyline = fetchDirections(origin, destination)
                polyline?.let {
                    drawRoute(it)
                }
            } catch (e: Exception) {
                Log.e("MapFragment", "Error fetching directions: ${e.message}")
                showError("Failed to fetch route")
            }
        }
    }

    private suspend fun fetchDirections(origin: LatLng, destination: LatLng): String? {
        return withContext(Dispatchers.IO) {
            try {
                val url = getDirectionsUrl(origin, destination)
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (!response.isSuccessful || responseBody == null) {
                    throw IOException("Failed to fetch directions: ${response.message}")
                }

                val jsonResponse = JSONObject(responseBody)
                val routes = jsonResponse.getJSONArray("routes")
                if (routes.length() > 0) {
                    val route = routes.getJSONObject(0)
                    val overviewPolyline = route.getJSONObject("overview_polyline")
                    overviewPolyline.getString("points")
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("MapFragment", "Error in fetchDirections: ${e.message}")
                null
            }
        }
    }

    private fun getDirectionsUrl(origin: LatLng, destination: LatLng): String {
        return "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origin.latitude},${origin.longitude}" +
                "&destination=${destination.latitude},${destination.longitude}" +
                "&key=$apiKey"
    }

    private fun drawRoute(polyline: String) {
        try {
            val decodedPath = PolyUtil.decode(polyline)
            _googleMap?.addPolyline(
                PolylineOptions()
                    .addAll(decodedPath)
                    .color(ContextCompat.getColor(requireContext(), R.color.theme_color))
                    .width(10f)
            )
            showSuccess("Route drawn successfully!")
        } catch (e: Exception) {
            Log.e("MapFragment", "Error drawing route: ${e.message}")
            showError("Failed to draw route")
        }
    }

    private fun showError(message: String) {
        view?.let {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSuccess(message: String) {
        view?.let {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            enableMyLocation()
        } else {
            showError("Location permission denied. Unable to show user location.")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _googleMap = null
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
package com.ritika.voy.home

import android.Manifest
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.location.Location
import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.ritika.voy.R.id.map_fragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.ritika.voy.BuildConfig
import com.ritika.voy.MainActivity
import com.ritika.voy.R
import com.ritika.voy.api.ApiService
import com.ritika.voy.api.DataStoreManager
import com.ritika.voy.api.RetrofitInstance
import com.ritika.voy.api.RetrofitInstance.mapApi
import com.ritika.voy.api.dataclasses.AvailableRides
import com.ritika.voy.api.dataclasses.AvailableRidesSearchRequest
import com.ritika.voy.api.dataclasses.LocationPoint
import com.ritika.voy.api.dataclasses.mapsDataClasses.OriginDestination
import com.ritika.voy.api.dataclasses.mapsDataClasses.Route
import com.ritika.voy.api.dataclasses.mapsDataClasses.RouteModifiers
import com.ritika.voy.api.dataclasses.mapsDataClasses.RoutesRequest
import com.ritika.voy.databinding.ActivityMapBinding
import com.ritika.voy.geocoding_helper.GeocodingHelper
import com.ritika.voy.geocoding_helper.GeocodingResult
import com.ritika.voy.mapRoute.drawRouteOnMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.log

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private var firstMarkerLatLng: LatLng? = null
    private var secondMarkerLatLng: LatLng? = null
    private lateinit var markerIcon: BitmapDescriptor
    private var isSecondMarkerAllowed = false
    private var isRouteAllowed = false
    private var firstMarker: Marker? = null
    private var secondMarker: Marker? = null
    private var selectedButtonValue: String? = null
    private var previousButton: Button? = null
    private lateinit var geocodingHelper: GeocodingHelper
    private var startLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null
    private var startLocation: String? = null
    private var destinationLocation: String? = null
    private var hasSetInitialLocations = false
    private lateinit var role: String
    private lateinit var authToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Initialize geocodingHelper
        geocodingHelper = GeocodingHelper(this)

        // Get data from Bundle
        val bundle = intent.extras
        startLocation = bundle?.getString("startLocation")
        destinationLocation = bundle?.getString("destinationLocation")
        role = bundle?.getString("role") ?: "passenger"
        Toast.makeText(this, "Your role is $role", Toast.LENGTH_SHORT).show()
        Log.d("Bundle", " Bundle:  from  $startLocation to $destinationLocation")

        //calling geocoding to convert address in string to LatLng
        lifecycleScope.launch {
            try {
                startLatLng = geoCodeAddress(startLocation ?: "")
                destinationLatLng = geoCodeAddress(destinationLocation ?: "")
                Log.d("geocode-address", "geocodeAddress: from $startLatLng to $destinationLatLng")

                // Set initial markers if both locations are available
                if (startLatLng != null && destinationLatLng != null && !hasSetInitialLocations) {
                    setInitialLocations()
                }
            } catch (e: Exception) {
                Log.e("geocode-address", "Error geocoding: ${e.message}")
            }
        }

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.MAP_API_KEY)
        }

        setupSearchBox()

        markerIcon = getBitmapDescriptorFromResource(R.drawable.location)
//
//        binding.logoutButton.setOnClickListener {
//            lifecycleScope.launch {
//                DataStoreManager.clearTokens(applicationContext)
//            }
//            val intent = Intent(this, MainActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(intent)
//            finish()
//        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission()

        binding.satellite.setOnCheckedChangeListener { _, isChecked ->
            googleMap.mapType = if (isChecked) {
                GoogleMap.MAP_TYPE_HYBRID
            } else {
                GoogleMap.MAP_TYPE_NORMAL
            }
        }

        binding.confirmButton.setOnClickListener {
            when {
                firstMarkerLatLng == null -> {
                    showAddFirstLocationDialog()
                }

                firstMarkerLatLng != null && secondMarkerLatLng == null && !isSecondMarkerAllowed -> {
                    isSecondMarkerAllowed = true
                    binding.descText.text = "Drop"
                }

                firstMarkerLatLng != null && secondMarkerLatLng == null && isSecondMarkerAllowed -> {
                    showAddSecondLocationDialog()
                }

                firstMarkerLatLng != null && secondMarkerLatLng != null -> {
                    isRouteAllowed = true
                    getOptimalRoute()

                    binding.confirmButton.visibility = View.GONE
                    binding.satellite.visibility = View.GONE
                    binding.descText.visibility = View.GONE
                    binding.backButton.visibility = View.VISIBLE
                    binding.bottomWidget.visibility = View.VISIBLE
                    binding.whiteBar.visibility = View.VISIBLE
                    binding.bottomWidgetText.visibility = View.VISIBLE
                    binding.button1.visibility = View.VISIBLE
                    binding.button2.visibility = View.VISIBLE
                    binding.button3.visibility = View.VISIBLE
                    binding.button4.visibility = View.VISIBLE
                    binding.button5.visibility = View.VISIBLE
                    binding.proceedButton.visibility = View.VISIBLE
                    binding.routeView.root.visibility = View.VISIBLE

                    Log.d(
                        "reverseGeoCoding",
                        "geocoding while setOnMap $firstMarkerLatLng $secondMarkerLatLng"
                    )

                    lifecycleScope.launch {
                        try {
                            if (firstMarkerLatLng != null && secondMarkerLatLng != null) {
                                val startAddress = reverseGeoCode(
                                    firstMarkerLatLng!!.latitude, firstMarkerLatLng!!.longitude
                                )
                                val destinationAddress = reverseGeoCode(
                                    secondMarkerLatLng!!.latitude, secondMarkerLatLng!!.longitude
                                )
                                Log.d(
                                    "reverseGeoCodeAddress",
                                    "reverseGeoCodeAddress: from $startAddress to $destinationAddress"
                                )
                                binding.routeView.root.findViewById<TextView>(R.id.start_address)?.text =
                                    startAddress
                                binding.routeView.root.findViewById<TextView>(R.id.drop_address)?.text =
                                    destinationAddress
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "reverseGeoCodeAddress",
                                "Error during reverse geocoding: ${e.message}"
                            )
                        }
                    }
                }
            }
        }

        binding.backButton.setOnClickListener {
            // Reset all state variables
            binding.confirmButton.visibility = View.VISIBLE
            binding.satellite.visibility = View.VISIBLE
            binding.descText.visibility = View.VISIBLE
            binding.backButton.visibility = View.GONE
            binding.bottomWidget.visibility = View.GONE
            binding.whiteBar.visibility = View.GONE
            binding.bottomWidgetText.visibility = View.GONE
            binding.button1.visibility = View.GONE
            binding.button2.visibility = View.GONE
            binding.button3.visibility = View.GONE
            binding.button4.visibility = View.GONE
            binding.button5.visibility = View.GONE
            binding.proceedButton.visibility = View.GONE
            binding.routeView.root.visibility = View.GONE

            // Clear the map
            googleMap.clear()

            // Reset all markers and state flags
            firstMarkerLatLng = null
            secondMarkerLatLng = null
            isSecondMarkerAllowed = false
            isRouteAllowed = false

            // Reset the description text to initial state
            binding.descText.text = "Pickup"
        }
        binding.proceedButton.setOnClickListener {

            if (selectedButtonValue != null) {
                Toast.makeText(this, "$selectedButtonValue", Toast.LENGTH_SHORT).show()
                lifecycleScope.launch {
                    DataStoreManager.getToken(this@MapActivity, "access").first()!!.let {
                        authToken = it
                        if (authToken != "") {
                            proceedButtonClicked(authToken)
                        }
                    }
                }
            } else {
                Toast.makeText(
                    applicationContext, "Please select the no. of seats", Toast.LENGTH_SHORT
                ).show()
            }
        }
        setupButtonListeners()

//        binding.cancelButton.setOnClickListener {
//            Toast.makeText(
//                applicationContext, "navigation to Map Fragment", Toast.LENGTH_SHORT
//            ).show()
//            val mapFragment = MapFragment()
//            supportFragmentManager.beginTransaction().replace(R.id.main, mapFragment)
//                .addToBackStack(null).commit()
//        }
    }

    private fun findingRidesScreen() {
//        setupLoaderBackground()
        adjustConstraints()
        toggleViewVisibility()
        setRouteViewBackground()
        adjustRouteViewMargins()
        updateRouteViewTextColors()
    }


    private fun setupLoaderBackground() {
        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(
                ContextCompat.getColor(this, android.R.color.white),
                ContextCompat.getColor(this, android.R.color.holo_green_light)
            )
        )
        binding.loader.background = gradientDrawable
        startColorAnimation(gradientDrawable)
    }

    private fun adjustConstraints() {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.root)

        constraintSet.connect(
            binding.bottomWidget.id,
            ConstraintSet.TOP,
            binding.guideline8.id,
            ConstraintSet.BOTTOM
        )

        TransitionManager.beginDelayedTransition(binding.root)

        constraintSet.connect(
            binding.routeView.root.id,
            ConstraintSet.TOP,
            binding.cancelButton.id,
            ConstraintSet.BOTTOM
        )
        constraintSet.connect(
            binding.routeView.root.id,
            ConstraintSet.START,
            binding.guideline2.id,
            ConstraintSet.START
        )
        constraintSet.applyTo(binding.root)
    }

    private fun toggleViewVisibility() {
        listOf(
            binding.bottomWidgetText,
            binding.button1,
            binding.button2,
            binding.button3,
            binding.button4,
            binding.button5,
            binding.proceedButton
        ).forEach { it.visibility = View.GONE }

        binding.findingRidersText.visibility = View.VISIBLE
        binding.findingRidersDesc.visibility = View.VISIBLE
        binding.loader.visibility = View.VISIBLE
        binding.riderImage.visibility = View.VISIBLE
        binding.cancelButton.visibility = View.VISIBLE
        binding.backButton.visibility = View.GONE
        binding.routeView.swapButton.visibility = View.GONE
    }

    private fun setRouteViewBackground() {
        val newBackground = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_enabled), GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(Color.parseColor("#3d3d3d"))
                cornerRadius = 16f * resources.displayMetrics.density
            })
        }
        binding.routeView.root.background = newBackground
    }

    private fun adjustRouteViewMargins() {
        val marginPx = 16f * resources.displayMetrics.density
        val params = binding.routeView.root.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = marginPx.toInt()
        binding.routeView.root.layoutParams = params
    }

    private fun updateRouteViewTextColors() {
        binding.routeView.root.findViewById<TextView>(R.id.start_address)
            ?.setTextColor(Color.parseColor("#ccc7eb"))
        binding.routeView.root.findViewById<TextView>(R.id.drop_address)
            ?.setTextColor(Color.parseColor("#ccc7eb"))
    }

    private fun startLoaderAnimation() {
        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(
                ContextCompat.getColor(this, android.R.color.white),
                ContextCompat.getColor(this, android.R.color.holo_green_light)
            )
        )
        binding.loader.background = gradientDrawable

        val animator = ObjectAnimator.ofFloat(0f, 1f).apply {
            duration = 1500
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()

            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float
                gradientDrawable.setGradientCenter(fraction, 0.5f)
            }
        }
        animator.start()
    }

    private fun proceedButtonClicked(authToken: String) {
        setupLoaderBackground() // Start loader background
        findingRidesScreen() // Adjust UI for fetching state

        // Fetch API
        lifecycleScope.launch {
            startLoaderAnimation() // Start loader animation
            val response = fetchApiResponse(authToken) // Pass the auth token
            if (response != null) {
                onApiSuccess(response)
            } else {
                onApiFailure()
            }
        }
    }

    private suspend fun fetchApiResponse(authToken: String): AvailableRides? {
        val requestBody = AvailableRidesSearchRequest(
            pickup_point = LocationPoint(
                type = "Point",
                coordinates = listOf(startLatLng!!.latitude, startLatLng!!.longitude)
            ),
            destination_point = LocationPoint(
                type = "Point",
                coordinates = listOf(destinationLatLng!!.latitude, destinationLatLng!!.longitude)
            ),
            seats_needed = selectedButtonValue!!.toInt(),
            radius = 5000.0
        )

        return withContext(Dispatchers.IO) {
            try {
                val response =
                    RetrofitInstance.api.getAvailableRides("Bearer $authToken", requestBody)
                if (response != null && response.success) {
                    response // Return the response if successful
                } else {
                    null // Return null if the response is unsuccessful
                }
            } catch (e: Exception) {
                Log.d("API", "fetchApiResponse: $e")
                e.printStackTrace()
                null // Return null in case of an error
            }
        }
    }


    private fun onApiSuccess(availableRides: AvailableRides) {
        // Handle the successful API response
        availableRides.data?.let { rides ->
            // Process and display the list of rides
            for (ride in rides) {
                println("Ride from ${ride.data.start_location} to ${ride.data.end_location}")
                Log.d("API", "onApiSuccess:  ${availableRides.data} ")
            }
        }
        // Update the loader to 100% completion
        val gradientDrawable = binding.loader.background as GradientDrawable
        gradientDrawable.colors = intArrayOf(
            ContextCompat.getColor(this, android.R.color.holo_green_light),
            ContextCompat.getColor(this, android.R.color.holo_green_light)
        )
        binding.loader.background = gradientDrawable
        sendResponseStatus(true)
    }

    private fun onApiFailure() {
        // Reset the loader animation and notify failure if needed
        binding.loader.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
        sendResponseStatus(false)
    }

    private fun sendResponseStatus(status: Boolean) {
        // Send the response status (e.g., navigate to the next screen or show a toast)
        if (status) {
            Toast.makeText(this, "API call successful", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "API call failed", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun geoCodeAddress(address: String): LatLng? {
        try {
            val result = geocodingHelper.geocodeAddress(address)
            return if (result.success) {
                Log.d(
                    "Geocoding", """
                    Latitude: ${result.latitude}
                    Longitude: ${result.longitude}
                    Formatted Address: ${result.formattedAddress}
                """.trimIndent()
                )
                LatLng(result.latitude, result.longitude)
            } else {
                Log.e("Geocoding", "Failed to geocode address: ${result.errorMessage}")
                handleUnknownLocation(address)
            }
        } catch (e: Exception) {
            Log.e("Geocoding", "Exception during geocoding: ${e.message}")
            return handleUnknownLocation(address)
        }
    }

    private suspend fun handleUnknownLocation(address: String): LatLng? {
        // First try to get the last known location as a reference point
        var referenceLocation: LatLng? = null
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                val location = getLastLocationSuspend()
                if (location != null) {
                    referenceLocation = LatLng(location.latitude, location.longitude)
                }
            } catch (e: Exception) {
                Log.e("Location", "Error getting last location: ${e.message}")
            }
        }

        // If we have a reference location, try to find nearby addresses
        referenceLocation?.let { location ->
            try {
                val nearbyResults = geocodingHelper.findNearbyAddresses(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    radiusKm = 2.0, // Search within 1km
                    maxResults = 5
                )

                // Find the most relevant nearby address based on similarity to the original address
                val mostRelevantAddress = findMostRelevantAddress(address, nearbyResults)

                mostRelevantAddress?.let {
                    return LatLng(it.latitude, it.longitude)
                }
            } catch (e: Exception) {
                Log.e("NearbySearch", "Error finding nearby addresses: ${e.message}")
            }
        }

        // If we still haven't found a location, show an error dialog on the main thread
        withContext(Dispatchers.Main) {
            showLocationNotFoundDialog(address)
        }

        return null
    }

    // Add this new suspend function to handle getting last location
    private suspend fun getLastLocationSuspend(): Location? = suspendCoroutine { continuation ->
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            continuation.resume(null)
            return@suspendCoroutine
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            continuation.resume(location)
        }.addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }.addOnCanceledListener {
            continuation.resume(null)
        }
    }


    private fun findMostRelevantAddress(
        originalAddress: String,
        nearbyResults: List<GeocodingResult>,
    ): GeocodingResult? {
        if (nearbyResults.isEmpty()) return null

        // Simple string similarity comparison
        return nearbyResults.maxByOrNull { result ->
            calculateAddressSimilarity(
                originalAddress.lowercase(), result.formattedAddress?.lowercase() ?: ""
            )
        }
    }

    private fun calculateAddressSimilarity(original: String, candidate: String): Int {
        // Split addresses into words and count matching words
        val originalWords = original.split(" ").toSet()
        val candidateWords = candidate.split(" ").toSet()
        return originalWords.intersect(candidateWords).size
    }

    private fun showLocationNotFoundDialog(address: String) {
        val dialog = AlertDialog.Builder(this).setTitle("Location Not Found").setMessage(
            "We couldn't find \"$address\". Would you like to:\n\n" + "1. Pick a location on the map\n" + "2. Try a different address"
        ).setPositiveButton("Pick on Map") { dialog, _ ->
            dialog.dismiss()
            // Enable map click handling
            binding.descText.text = if (firstMarkerLatLng == null) "Pickup" else "Drop"
        }.setNegativeButton("Try Again") { dialog, _ ->
            dialog.dismiss()
            // Open the search box
            binding.searchBar.requestFocus()
        }.create()

        dialog.show()
    }

    private suspend fun reverseGeoCode(latitude: Double, longitude: Double): String? {
        return try {
            val result = geocodingHelper.reverseGeocode(latitude, longitude)
            if (result.success) {
                Log.d("Reverse Geocoding", "Address: ${result.formattedAddress}")
                result.formattedAddress
            } else {
                Log.e("Reverse Geocoding", "Failed to reverse geocode: ${result.errorMessage}")
                "Unknown address" // Return a fallback value
            }
        } catch (e: Exception) {
            Log.e("Reverse Geocoding", "Exception during reverse geocoding: ${e.message}")
            "Error retrieving address" // Return an error message
        }
    }


    private fun nearbyAddresses(latitude: Double, longitude: Double) {
        lifecycleScope.launch {
            val results = geocodingHelper.findNearbyAddresses(
                latitude = latitude, longitude = longitude, radiusKm = 0.5, maxResults = 5
            )
            results.forEach { result ->
                Log.d("Nearby", "Found: ${result.formattedAddress}")
            }
        }
    }


    private fun isLocationInIndia(latLng: LatLng): Boolean {
        return latLng.latitude in INDIA_BOUNDS.southLat..INDIA_BOUNDS.northLat && latLng.longitude in INDIA_BOUNDS.westLng..INDIA_BOUNDS.eastLng
    }

    private fun showLocationOutsideIndiaDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Location Not Supported")
            .setMessage("Selected location is outside India. Please select a location within India.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun showAddFirstLocationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this).setTitle("First Location Missing")
            .setMessage("Please add the first location on the map.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun showAddSecondLocationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Second Location Missing")
            .setMessage("Please add the second location on the map.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun setupButtonListeners() {
        val buttons = listOf(
            binding.button1, binding.button2, binding.button3, binding.button4, binding.button5
        )
        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                previousButton?.let {
                    it.setBackgroundResource(R.drawable.seats_background)
                }
                selectedButtonValue = (index + 1).toString()
                val drawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = resources.getDimension(R.dimen.button_corner_radius)
                    setColor(ContextCompat.getColor(this@MapActivity, R.color.theme_color))
                }
                button.background = drawable

                previousButton = button
            }
        }
    }

    private fun startColorAnimation(drawable: Drawable?) {
        if (drawable is GradientDrawable) {
            val animator = ObjectAnimator.ofFloat(-1f, 1f).apply {
                duration = 1500
                repeatCount = ObjectAnimator.INFINITE
                interpolator = AccelerateDecelerateInterpolator()

                addUpdateListener { animation ->
                    val fraction = animation.animatedValue as Float

                    drawable.orientation = GradientDrawable.Orientation.LEFT_RIGHT

                    drawable.colors = intArrayOf(
                        if (fraction < 0f) Color.WHITE else ContextCompat.getColor(
                            this@MapActivity, android.R.color.holo_green_light
                        ), if (fraction < 0f) Color.WHITE else ContextCompat.getColor(
                            this@MapActivity, android.R.color.holo_green_light
                        )
                    )


                    drawable.setGradientCenter(fraction, 0.5f)
                }
            }
            animator.start()
        }
    }

    private fun setupSearchBox() {
        binding.searchBar.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {

                val fields = listOf(
                    Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS
                )

                val intent =
                    Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(this)
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
            }
        }

        // Optional: Keep the click listener for redundancy
        binding.searchBar.setOnClickListener {
            if (!binding.searchBar.hasFocus()) {
                binding.searchBar.requestFocus()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(it)
                        Log.i("HomeActivity", "Place: ${place.name}, ${place.latLng}")

                        place.latLng?.let { latLng ->
                            // Handle the selected place
                        }

                        // Clear focus and hide keyboard
                        binding.searchBar.clearFocus()
                        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(binding.searchBar.windowToken, 0)
                    }
                }

                AutocompleteActivity.RESULT_ERROR -> {
                    // Handle the error
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(it)
                        Log.e("HomeActivity", "Error: ${status.statusMessage}")
                    }
                    binding.searchBar.clearFocus()
                    binding.searchBar.setText("")
                }

                Activity.RESULT_CANCELED -> {
                    // Reset focus and clear any partial text
                    binding.searchBar.clearFocus()

                    // Hide keyboard
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.searchBar.windowToken, 0)
                }
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun getBitmapDescriptorFromResource(resourceId: Int): BitmapDescriptor {
        val bitmap = BitmapFactory.decodeResource(resources, resourceId)
        val scaledBitmap = Bitmap.createScaledBitmap(
            bitmap, (bitmap.width * 0.12).toInt(), (bitmap.height * 0.12).toInt(), false
        )
        return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getLastKnownLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastKnownLocation()
            } else {
                // Permission denied, handle accordingly
            }
        }
    }

    private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                }
            }
        }
    }

    private fun getOptimalRoute() {
        if (firstMarkerLatLng != null && secondMarkerLatLng != null && isRouteAllowed) {
            lifecycleScope.launch {
                val route = getRoute(
                    mapApi, firstMarkerLatLng!!, secondMarkerLatLng!!, BuildConfig.MAP_API_KEY
                )
                route?.let {
                    drawRouteOnMap(it, googleMap)
                }
            }
        }
    }

    suspend fun getRoute(
        apiService: ApiService,
        origin: LatLng,
        destination: LatLng,
        apiKey: String,
    ): Route? {
        val request = RoutesRequest(
            origin = OriginDestination(
                com.ritika.voy.api.dataclasses.mapsDataClasses.Location(
                    origin
                )
            ), destination = OriginDestination(
                com.ritika.voy.api.dataclasses.mapsDataClasses.Location(
                    destination
                )
            ), routeModifiers = RouteModifiers(
                avoidTolls = false, avoidHighways = false, avoidFerries = false
            )
        )
        val response = apiService.computeRoutes(request, apiKey)
        return response.routes.firstOrNull()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isMyLocationButtonEnabled = true

        val locationButton =
            (findViewById<View>(Integer.parseInt("1")).parent as View).findViewById<View>(
                Integer.parseInt("2")
            )
        val rlp = locationButton.layoutParams as (RelativeLayout.LayoutParams)

        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        rlp.setMargins(0, 0, 30, 30)

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style)
            )
            if (!success) {
                Log.e("HomeActivity", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("HomeActivity", "Can't find style. Error: ", e)
        }

        checkLocationPermission()

        googleMap.setOnMapClickListener { latLng ->
            binding.searchBar.clearFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.searchBar.windowToken, 0)

            if (isLocationInIndia(latLng)) {
                if (firstMarkerLatLng == null) {
                    firstMarkerLatLng = latLng
                    firstMarker =
                        googleMap.addMarker(MarkerOptions().position(latLng).icon(markerIcon))
                } else if (secondMarkerLatLng == null && isSecondMarkerAllowed) {
                    secondMarkerLatLng = latLng
                    secondMarker =
                        googleMap.addMarker(MarkerOptions().position(latLng).icon(markerIcon))
                    isSecondMarkerAllowed = false
                } else if (firstMarkerLatLng != null && secondMarkerLatLng == null) {
                    firstMarker?.remove()
                    firstMarkerLatLng = latLng
                    firstMarker =
                        googleMap.addMarker(MarkerOptions().position(latLng).icon(markerIcon))
                } else if (firstMarkerLatLng != null && secondMarkerLatLng != null && !isRouteAllowed) {
                    secondMarker?.remove()
                    secondMarkerLatLng = latLng
                    secondMarker =
                        googleMap.addMarker(MarkerOptions().position(latLng).icon(markerIcon))
                } else if (firstMarkerLatLng != null && secondMarkerLatLng != null && isRouteAllowed) {
                    // Do nothing if both markers are set and route is allowed
                    return@setOnMapClickListener
                }
            } else {
                showLocationOutsideIndiaDialog()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val AUTOCOMPLETE_REQUEST_CODE = 2

        // India's approximate bounding box
        private val INDIA_BOUNDS = object {
            val northLat = 35.7 // Northern-most point
            val southLat = 6.5  // Southern-most point
            val westLng = 68.1  // Western-most point
            val eastLng = 97.4  // Eastern-most point
        }
    }


    private fun setInitialLocations() {
        if (!hasSetInitialLocations && startLatLng != null) {
            firstMarkerLatLng = startLatLng
            firstMarker =
                googleMap.addMarker(MarkerOptions().position(startLatLng!!).icon(markerIcon))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLatLng!!, 15f))

            // Show confirmation dialog for proceeding to destination
            binding.confirmButton.setOnClickListener {
                showConfirmationDialog("Confirm Pickup Location",
                    "Do you want to confirm this pickup location?",
                    onConfirm = {
                        isSecondMarkerAllowed = true
                        binding.descText.text = "Drop"

                        // Set destination marker after pickup confirmation
                        if (destinationLatLng != null) {
                            secondMarkerLatLng = destinationLatLng
                            secondMarker = googleMap.addMarker(
                                MarkerOptions().position(destinationLatLng!!).icon(markerIcon)
                            )
                            googleMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    destinationLatLng!!, 15f
                                )
                            )
                            binding.confirmButton.setOnClickListener {

                                showConfirmationDialog("Confirm Drop Location",
                                    "Do you want to confirm this drop location?",
                                    onConfirm = {

                                        if (firstMarkerLatLng != null && secondMarkerLatLng != null) {
                                            isRouteAllowed = true
                                            getOptimalRoute()
                                            binding.confirmButton.visibility = View.GONE
                                            binding.satellite.visibility = View.GONE
                                            binding.descText.visibility = View.GONE
                                            binding.backButton.visibility = View.VISIBLE
                                            binding.bottomWidget.visibility = View.VISIBLE
                                            binding.whiteBar.visibility = View.VISIBLE
                                            binding.bottomWidgetText.visibility = View.VISIBLE
                                            binding.button1.visibility = View.VISIBLE
                                            binding.button2.visibility = View.VISIBLE
                                            binding.button3.visibility = View.VISIBLE
                                            binding.button4.visibility = View.VISIBLE
                                            binding.button5.visibility = View.VISIBLE
                                            binding.proceedButton.visibility = View.VISIBLE
                                            binding.routeView.root.visibility = View.VISIBLE
                                            Log.d(
                                                "hii",
                                                "onCreate: $firstMarkerLatLng $secondMarkerLatLng"
                                            )

                                            lifecycleScope.launch {
                                                try {
                                                    if (firstMarkerLatLng != null && secondMarkerLatLng != null) {
                                                        val startAddress = reverseGeoCode(
                                                            firstMarkerLatLng!!.latitude,
                                                            firstMarkerLatLng!!.longitude
                                                        )
                                                        val destinationAddress = reverseGeoCode(
                                                            secondMarkerLatLng!!.latitude,
                                                            secondMarkerLatLng!!.longitude
                                                        )
                                                        Log.d(
                                                            "reverseGeoCodeAddress",
                                                            "reverseGeoCodeAddress: from $startAddress to $destinationAddress"
                                                        )
                                                        binding.routeView.root.findViewById<TextView>(
                                                            R.id.start_address
                                                        )?.text = startAddress
                                                        binding.routeView.root.findViewById<TextView>(
                                                            R.id.drop_address
                                                        )?.text = destinationAddress
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e(
                                                        "reverseGeoCodeAddress",
                                                        "Error during reverse geocoding: ${e.message}"
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    onDeny = {
                                        // Clear the marker and allow user to select new location
                                        secondMarker?.remove()
                                        secondMarkerLatLng = null
                                        destinationLatLng = null
                                    })
                            }

                            binding.backButton.setOnClickListener {
                                startLocation = null
                                destinationLocation = null
                                val intent = intent
                                finish()
                                startActivity(intent)

                            }
                        }
                    },

                    onDeny = {
                        // Clear the marker and allow user to select new location
                        firstMarker?.remove()
                        firstMarkerLatLng = null
                        startLatLng = null

                    })
            }
            hasSetInitialLocations = true
        }
    }

    private fun showConfirmationDialog(
        title: String,
        message: String,
        onConfirm: () -> Unit,
        onDeny: () -> Unit,
    ) {
        androidx.appcompat.app.AlertDialog.Builder(this).setTitle(title).setMessage(message)
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                onConfirm()
            }.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                onDeny()
            }.show()
    }

}
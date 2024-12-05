package com.ritika.voy.home

import DataX
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.ritika.voy.R
import com.ritika.voy.api.DataStoreManager
import com.ritika.voy.api.RetrofitInstance
import com.ritika.voy.api.dataclasses.ErrorResponse
import com.ritika.voy.api.dataclasses.RideRequestResponse
import com.ritika.voy.geocoding_helper.GeocodingHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Address
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Locale

import kotlin.math.log
import kotlin.properties.Delegates

class RideDetailsBottomSheetFragment : BottomSheetDialogFragment() {
    private var rideDetails: DataX? = null
    private var TAG = "RideDetailsBottomSheetFragment"
    private lateinit var geocodingHelper: GeocodingHelper
    private lateinit var startAddress: String
    private lateinit var destinationAddress: String
    private var startLocation: LatLng? = null
    private var endLocation: LatLng? = null
    private var seats: String = "1"
    private var id: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        geocodingHelper = GeocodingHelper(requireContext())
        arguments?.let {
            rideDetails = it.getParcelable("rideDetails")
            if (startLocation == null && endLocation == null) {
                startLocation = it.getParcelable("start_location")
                endLocation = it.getParcelable("end_location")
                seats = it.getString("seats").toString()
            }
            Log.d("BundleData", "onCreate: $startLocation $endLocation")
            lifecycleScope.launch {
                val progressDialog = ProgressDialog(requireContext())
                progressDialog.setMessage("Loading...")
                progressDialog.setCancelable(false)
                progressDialog.show()
                try {
                    if (startLocation != null && endLocation != null) {
                        startAddress = reverseGeoCode(
                            startLocation!!.latitude, startLocation!!.longitude
                        ).toString()
                        destinationAddress = reverseGeoCode(
                            endLocation!!.latitude, endLocation!!.longitude
                        ).toString()
                        Log.d(
                            TAG,
                            "reverseGeoCodeAddress: from $startAddress to $destinationAddress"
                        )
                    }
                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "Error during reverse geocoding: ${e.message}"
                    )
                } finally {
                    Log.d(TAG, "Finally block executed")
                    progressDialog.dismiss()
                }
            }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_ride_details_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rideDetails?.let {
            val (time, dateFormatted) = convertDateTime(it.start_time)
            Log.d(TAG, "DateAndTime: $time, $dateFormatted")

            // Populate the BottomSheet views with `rideDetails`
            view.findViewById<TextView>(R.id.driver_name).text = it.driver_name
            view.findViewById<TextView>(R.id.rating).text = it.driver_rating.toString()
            view.findViewById<TextView>(R.id.car_modal).text = it.car_details.car_modal
            view.findViewById<TextView>(R.id.start_address).text = it.start_location
            view.findViewById<TextView>(R.id.drop_address).text = it.end_location
            view.findViewById<TextView>(R.id.start_time).text = time
            view.findViewById<TextView>(R.id.no_of_seats).text =
                "Max ${it.available_seats.toString()} in the back"
            view.findViewById<TextView>(R.id.date).text = dateFormatted
            val profile: ImageView = view.findViewById(R.id.profile_photo)
            id = it.id
            val imageUrl = it.driver_profile_photo
            Log.d(TAG, "Image:  $imageUrl")
            try {
                Glide.with(requireContext()).load(imageUrl).placeholder(R.drawable.profile_image)
                    .error(R.drawable.profile_image).transform(CircleCrop()).into(profile)
            } catch (e: Exception) {
                Log.d(TAG, "onLoadCleared:  $e")
            }

            val requestRideButton = view.findViewById<TextView>(R.id.request_ride_button)
            requestRideButton.setOnClickListener {
                lifecycleScope.launch {
                    val progressDialog = ProgressDialog(requireContext())
                    progressDialog.setMessage("Loading...")
                    progressDialog.setCancelable(false)
                    progressDialog.show()
                    try {
                        val authToken: String =
                            DataStoreManager.getToken(requireContext(), "access").first().toString()
                        Log.d(
                            TAG,
                            "onViewCreated: $authToken, ${id}, $startAddress, $destinationAddress, $startLocation, $endLocation, $seats"
                        )
                        val response = fetchRideRequestApiResponse(
                            "$authToken",
                            it.id,
                            startAddress,
                            destinationAddress,
                            startLocation!!,
                            endLocation!!,
                            seats.toInt()
                        )
                        if (response != null) {
                            if (response.success) {
                                Log.d(TAG, "Ride request successful: ${response.data}")
                            } else {
                                Log.e(TAG, "Ride request failed: ${response.message}")
                            }
                        } else {
                            Log.e(TAG, "Ride request failed: ${response?.message}")
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "Error: $e")
                    } finally {
                        progressDialog.dismiss()
                    }
                }
            }
            Log.d(TAG, "DateAndTime: ${it.start_time}")
            // Add more fields as necessary
        }
    }

    fun convertDateTime(input: String): Pair<String, String> {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputTimeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            val outputDateFormat = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())

            val date = inputFormat.parse(input)

            val time = outputTimeFormat.format(date)
            val dateFormatted = outputDateFormat.format(date)

            return Pair(time, dateFormatted)
        } catch (e: Exception) {
            Log.d("DateAndTime", "convertDateTime: $e")
        }
        return Pair("", "")
    }


    companion object {
        fun newInstance(rideDetails: DataX, bundle: Bundle): RideDetailsBottomSheetFragment {
            val fragment = RideDetailsBottomSheetFragment()
            val args = Bundle().apply {
                putParcelable("rideDetails", rideDetails)
                putParcelable("start_location", bundle.getParcelable("start_location"))
                putParcelable("end_location", bundle.getParcelable("end_location"))
                putString("seats", bundle.getString("seats"))
            }
            fragment.arguments = args
            return fragment
        }
    }

    private suspend fun fetchRideRequestApiResponse(
        authToken: String,
        id: Int,
        startAddress: String,
        endAddress: String,
        startLocation: LatLng,
        endLocation: LatLng,
        seats: Int,
    ): RideRequestResponse? {
        val rideRequestBody = mapOf(
            "pickup_location" to startAddress,
            "dropoff_location" to endAddress,
            "pickup_point" to mapOf(
                "type" to "Point",
                "coordinates" to listOf(startLocation.longitude, startLocation.latitude)
            ),
            "dropoff_point" to mapOf(
                "type" to "Point",
                "coordinates" to listOf(endLocation.longitude, endLocation.latitude)
            ),
            "seats_needed" to seats
        )

        // Convert the Map to JsonObject
        val gson = Gson()
        val jsonObject = gson.toJsonTree(rideRequestBody).asJsonObject

        Log.d("API", "Sending ride request: $jsonObject")
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.requestRide(
                    authHeader = "Bearer $authToken",
                    passengerId = id,
                    rideRequest = jsonObject
                )

                if (response.success) {
                    Log.d("API", "Ride request successful: ${response.data}")
                    response
                } else {
                    Log.e("API", "Ride request failed: ${response.message}")
                    null
                }

            } catch (e: HttpException) {
                if (e.code() == 400) {
                    // If it's a 400 error, fetch the error body
                    val errorBody = e.response()?.errorBody()
                    val errorMessage = errorBody?.string()

                    // Deserialize the error message
                    val errorResponse = try {
                        Gson().fromJson(errorMessage, ErrorResponse::class.java)
                    } catch (ex: Exception) {
                        null
                    }

                    // Log the error body
                    Log.e(
                        "API",
                        "Bad request (400): ${errorResponse?.non_field_errors?.joinToString(", ")}"
                    )
                    null
                } else {
                    Log.e("API", "Exception during API call: ${e.localizedMessage}", e)
                    null
                }
            } catch (e: Exception) {
                Log.e("API", "Exception during API call: ${e.localizedMessage}", e)
                null
            }
        }
    }

    private suspend fun reverseGeoCode(latitude: Double, longitude: Double): String? {
        return try {
            val result = geocodingHelper.reverseGeocode(latitude, longitude)
            if (result.success) {
                Log.d(TAG, " reverse Coding Address: ${result.formattedAddress}")
                result.formattedAddress
            } else {
                Log.e(TAG, "Failed to reverse geocode: ${result.errorMessage}")
                "Unknown address" // Return a fallback value
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during reverse geocoding: ${e.message}")
            "Error retrieving address" // Return an error message
        }
    }

}

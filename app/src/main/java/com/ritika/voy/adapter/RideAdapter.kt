package com.ritika.voy.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import retrofit2.HttpException
import okhttp3.ResponseBody
import com.ritika.voy.R
import com.ritika.voy.api.RetrofitInstance
import com.ritika.voy.api.dataclasses.Data
import com.ritika.voy.api.dataclasses.ErrorResponse
import com.ritika.voy.api.dataclasses.RideRequestResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RideAdapter(
    private var rides: List<Data>,
    private val authToken: String,
    private val onRideRequested: (RideRequestResponse?) -> Unit,
) : RecyclerView.Adapter<RideAdapter.RideViewHolder>() {

    // ViewHolder class to hold and bind view elements
    class RideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val startLocationTV: TextView = itemView.findViewById(R.id.start_location)
        val endLocationTV: TextView = itemView.findViewById(R.id.end_location)
        val startTimeTV: TextView = itemView.findViewById(R.id.start_time)
        val availableSeatsTV: TextView = itemView.findViewById(R.id.available_seats)
        val driverNameTV: TextView = itemView.findViewById(R.id.driver_name)

        fun bind(ride: Data) {
            startLocationTV.text = ride.data.start_location
            endLocationTV.text = ride.data.end_location
            startTimeTV.text = ride.data.start_time
            availableSeatsTV.text = "Available Seats: ${ride.data.available_seats}"
            driverNameTV.text = "Driver: ${ride.data.driver_name}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ride_item, parent, false)
        return RideViewHolder(view)
    }

    override fun onBindViewHolder(holder: RideViewHolder, position: Int) {
        val ride = rides[position]
        holder.bind(ride)

        // Set click listener
        holder.itemView.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val response =
                    fetchRideRequestApiResponse(authToken, rides[position].data.driver.toInt())
                onRideRequested(response)
            }
        }
    }

    override fun getItemCount() = rides.size

    // Method to update the list of rides
    fun updateRides(newRides: List<Data>) {
        this.rides = newRides
        notifyDataSetChanged()  // Notify adapter to refresh the list
    }

    // Method to clear the list of rides
    fun clearRides() {
        this.rides = emptyList()
        notifyDataSetChanged()  // Notify adapter to refresh the list
    }

    private suspend fun fetchRideRequestApiResponse(
        authToken: String,
        passengerId: Int,
    ): RideRequestResponse? {
        // Same implementation as before
        // Create the request body as a Map
        val rideRequestBody = mapOf(
            "pickup_location" to "Central Park",
            "dropoff_location" to "JFK Terminal 4",
            "pickup_point" to mapOf(
                "type" to "Point",
                "coordinates" to listOf(28.676932700000002, 77.5019882)
            ),
            "dropoff_point" to mapOf(
                "type" to "Point",
                "coordinates" to listOf(28.66469254331024, 77.48862951993942)
            ),
            "seats_needed" to 2
        )

        // Convert the Map to JsonObject
        val gson = Gson()
        val jsonObject = gson.toJsonTree(rideRequestBody).asJsonObject

        Log.d("API", "Sending ride request: $jsonObject")
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.requestRide(
                    authHeader = "Bearer $authToken",
                    passengerId = passengerId,
                    rideRequest = jsonObject // Pass the JsonObject here
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
}

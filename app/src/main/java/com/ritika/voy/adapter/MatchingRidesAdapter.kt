package com.ritika.voy.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.ritika.voy.R
import com.ritika.voy.api.DataStoreManager
import com.ritika.voy.api.RetrofitInstance
import com.ritika.voy.api.dataclasses.DataXXXX
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MatchingRidesAdapter(private var rides: List<DataXXXX>) :
    RecyclerView.Adapter<MatchingRidesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val passengerName: TextView = view.findViewById(R.id.passenger_name)
        val pickupLocation: TextView = view.findViewById(R.id.start_location)
        val dropoffLocation: TextView = view.findViewById(R.id.end_location)
        val rating: TextView = view.findViewById(R.id.rating)
        val acceptButton = view.findViewById<Button>(R.id.accept)
        val rejectButton = view.findViewById<Button>(R.id.reject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_matching_ride, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ride = rides[position].data
        val rideId = rides[position].data.id
        Log.d("MatchingMyRides", "onBindViewHolder: $rideId")
        holder.passengerName.text = ride.passenger_name
        holder.pickupLocation.text = ride.pickup_location
        holder.dropoffLocation.text = ride.dropoff_location
        holder.rating.text = ride.passenger_rating.toString()

        var flag = 0
        var lastClickTime = 0L // To track the last click time

        holder.acceptButton.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < 1000) { // Throttle clicks within 1 second
                return@setOnClickListener
            }
            lastClickTime = currentTime // Update last click time

            if (flag == 0) {
                Toast.makeText(holder.itemView.context, "Ride accepted", Toast.LENGTH_SHORT).show()
                try {
                    manageDriverRequest("accept", rideId, holder.itemView.context)
                } catch (e: Exception) {
                    Log.d("MatchingRidesAdapter", "Error: ${e.message}")
                }
                flag = 1
            } else {
                Toast.makeText(holder.itemView.context, "Ride already accepted", Toast.LENGTH_SHORT)
                    .show()
            }
            // Accept the ride
            Toast.makeText(holder.itemView.context, "Ride accepted", Toast.LENGTH_SHORT).show()
        }

        holder.rejectButton.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < 1000) { // Throttle clicks within 1 second
                return@setOnClickListener
            }
            lastClickTime = currentTime // Update last click time

            if (flag == 1) {
                try {
                    manageDriverRequest("reject", rideId, holder.itemView.context)
                } catch (e: Exception) {
                    Log.d("MatchingRidesAdapter", "Error: ${e.message}")
                }
                Toast.makeText(holder.itemView.context, "Ride rejected", Toast.LENGTH_SHORT).show()
                flag = 0
            } else {
                Toast.makeText(holder.itemView.context, "Ride already rejected", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }

    fun manageDriverRequest(action: String, requestId: Int, context: Context) {
        if (action != "accept" && action != "reject") {
            println("Invalid action: $action")
            return
        }

        // Create the actionBody
        val actionBody = JsonObject().apply {
            addProperty("action", action)
        }

        // Get the auth token from DataStore using context
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetch the token from DataStoreManager
                val authToken = DataStoreManager.getToken(context, "access").first()
                if (authToken.isNullOrEmpty()) {
                    Log.d("ManageRequest", "No auth token found")
                    return@launch
                }

                // Make the API call with the token
                val response = RetrofitInstance.api.driverManageRequest(
                    "Bearer $authToken",
                    requestId,
                    actionBody
                )

                // Log the response or handle it as needed
                Log.d("ManageRequest", "Request successfully handled. Response: ${response.success}")

            } catch (e: Exception) {
                // Log the error
                Log.d("ManageRequest", "Error: ${e.message}")
            }
        }
    }

    override fun getItemCount() = rides.size

    fun updateRides(newRides: List<DataXXXX>) {
        rides = newRides
        notifyDataSetChanged()
    }
}
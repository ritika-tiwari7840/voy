package com.ritika.voy.adapter

import android.app.AlertDialog
import android.app.ProgressDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ritika.voy.R
import com.ritika.voy.api.dataclasses.MyRideItem
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import android.graphics.Color
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.gson.JsonObject
import com.ritika.voy.api.DataStoreManager
import com.ritika.voy.api.RetrofitInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class MyRidesAdapter(
    private val onItemClickListener: OnItemClickListener? = null,
) : ListAdapter<MyRideItem, MyRidesAdapter.RideViewHolder>(RideDiffCallback()) {

    // Interface for item click events
    interface OnItemClickListener {
        fun onItemClick(ride: MyRideItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_ride_item, parent, false)
        return RideViewHolder(view, onItemClickListener)
    }

    override fun onBindViewHolder(holder: RideViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // ViewHolder class to bind each ride item to its views
    class RideViewHolder(
        itemView: View,
        private val onItemClickListener: OnItemClickListener?,
    ) : RecyclerView.ViewHolder(itemView) {
        private val startLocationText: TextView = itemView.findViewById(R.id.start_location)
        private val endLocationText: TextView = itemView.findViewById(R.id.end_location)
        private val startTimeText: TextView = itemView.findViewById(R.id.date_time_picker)
        private val statusText: TextView = itemView.findViewById(R.id.status)

        private var currentRide: MyRideItem? = null

        init {
            val button = itemView.findViewById<Button>(R.id.see_who_matches)

        }


        fun bind(ride: MyRideItem) {
            val TAG: String = "MyRidesAdapter"
            val timeInISO = convertUTCToISO(ride.startTime)
            Log.d("Time", "$timeInISO")
            val time = formatDateTime(timeInISO)
            Log.d("Time", " $time")
            currentRide = ride
            startLocationText.text = ride.startLocation
            endLocationText.text = ride.endLocation
            startTimeText.text = time
            statusText.text = ride.status


            statusText.setOnClickListener {
                // Define states
                val states = listOf(
                    "Pending" to "#F28B82",
                    "Completed" to "#46A14B",
                    "Ongoing" to "#FFD700" // Yellow
                )
                val currentIndex = statusText.tag as? Int ?: 0
                val nextIndex = (currentIndex + 1) % states.size
                val (status, color) = states[nextIndex]
                statusText.text = status
                statusText.setTextColor(Color.parseColor(color))
                statusText.tag = nextIndex
                val builder = AlertDialog.Builder(itemView.context)
                builder.setTitle("Ride Status")
                builder.setMessage("Your ride is $status")
                builder.setPositiveButton("OK") { dialog, _ ->
                    val context = itemView.context
                    if (context is LifecycleOwner) {
                        context.lifecycleScope.launch {
                            val progressDialog = ProgressDialog(itemView.context)
                            progressDialog.setMessage("Loading...")
                            progressDialog.setCancelable(false)
                            progressDialog.show()
                            try {
                                val authToken = DataStoreManager.getToken(context, "access").first()
                                if (authToken.isNullOrEmpty()) {
                                    throw Exception("Authentication token is null or empty")
                                }
                                if (status == "Completed") {
                                    updateStatus("Bearer $authToken", "COMPLETED", currentRide?.id)
                                } else if (status == "Ongoing") {
                                    updateStatus("Bearer $authToken", "ONGOING", currentRide?.id)
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    TAG,
                                    "Error fetching token or updating status: ${e.message}"
                                )
                            } finally {
                                dialog.dismiss()
                                progressDialog.dismiss()
                            }
                        }
                    } else {
                        Log.e(
                            TAG,
                            "Context is not a LifecycleOwner. Unable to launch coroutine."
                        )
                        dialog.dismiss()
                    }
                }

                builder.show()
            }
        }

        private fun formatDateTime(dateTime: String?): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("EEE, MMM dd, hh:mm a", Locale.getDefault())
                val date = inputFormat.parse(dateTime ?: return "")
                outputFormat.format(date ?: return "")
            } catch (e: Exception) {
                e.printStackTrace()
                dateTime ?: ""
            }
        }

        private suspend fun updateStatus(authToken: String, status: String, rideId: Int?) {
            val TAG: String = "MyRidesAdapter"
            if (rideId == null) {
                Log.e("Status", "Ride ID cannot be null.")
                return
            }

            try {
                val updateRequest = JsonObject().apply {
                    addProperty("status", status)
                }

                Log.d(TAG, "Updating status to $status for ride $rideId")
                val response = RetrofitInstance.api.driverUpdateRideStatus(
                    authHeader = authToken,
                    requestId = rideId,
                    updateRequest = updateRequest
                )
                if (response.success) {
                    Log.d(TAG, "Status update successful: ${response.data.message}")
                    Toast.makeText(itemView.context, "${response.data.message}", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Log.e(TAG, "Error updating status: ${response.data.message}")
                    Toast.makeText(itemView.context, "${response.data.message}", Toast.LENGTH_SHORT)
                        .show()
                }
                Log.d(TAG, "Status update successful: $response")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating status: ${e.message}")
            }
        }

        fun convertUTCToISO(utcDate: String): String {
            return try {
                val utcFormat =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                val isoFormat =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
                        timeZone = TimeZone.getDefault()
                    }
                val date = utcFormat.parse(utcDate)
                isoFormat.format(date ?: return "")
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }

    }

    private class RideDiffCallback : DiffUtil.ItemCallback<MyRideItem>() {
        override fun areItemsTheSame(oldItem: MyRideItem, newItem: MyRideItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MyRideItem, newItem: MyRideItem): Boolean {
            return oldItem == newItem
        }
    }
}
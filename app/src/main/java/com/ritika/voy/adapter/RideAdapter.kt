package com.ritika.voy.adapter

import CarDetails
import DataX
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import retrofit2.HttpException
import com.ritika.voy.R
import com.ritika.voy.api.RetrofitInstance
import com.ritika.voy.api.dataclasses.Data
import com.ritika.voy.api.dataclasses.ErrorResponse
import com.ritika.voy.api.dataclasses.RideRequestResponse
import com.ritika.voy.home.RideDetailsBottomSheetFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class RideAdapter(
    private var rides: List<Data>,
    private val authToken: String,
    val bundle: Bundle,
    private val onRideRequested: (RideRequestResponse?) -> Unit,

    ) : RecyclerView.Adapter<RideAdapter.RideViewHolder>() {
    // ViewHolder class to hold and bind view elements
    class RideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val startLocationTV: TextView = itemView.findViewById(R.id.start_location)
        val endLocationTV: TextView = itemView.findViewById(R.id.end_location)
        val startTimeTV: TextView = itemView.findViewById(R.id.time)
        val rating: TextView = itemView.findViewById(R.id.rating)
        val driverNameTV: TextView = itemView.findViewById(R.id.driver_name)
        val modal: TextView = itemView.findViewById(R.id.car_modal)
        val profile: ImageView = itemView.findViewById(R.id.profile_photo)

        fun bind(ride: Data) {
            startLocationTV.text = ride.data.start_location
            endLocationTV.text = ride.data.end_location
            val timeInIso = convertUtcToLocalTime(
                ride.data.start_time, "yyyy-MM-dd'T'HH:mm:ss'Z'"
            )
            val (time, dateFormatted) = convertDateTime(timeInIso)
            startTimeTV.text = time
            rating.text = ride.data.driver_rating.toString()
            driverNameTV.text = ride.data.driver_name
            modal.text = ride.data.car_details.car_modal

            Log.d("API", "bind: ${ride.data.driver_profile_photo}")

            val imageUrl = ride.data.driver_profile_photo
            Log.d("Image", "onLoadCleared:  $imageUrl")
            try {
                Glide.with(itemView.context).load(imageUrl).placeholder(R.drawable.profile_image)
                    .error(R.drawable.profile_image).transform(CircleCrop()).into(profile)
            } catch (e: Exception) {
                Log.d("Image", "onLoadCleared:  $e")
            }
        }

        private fun convertUtcToLocalTime(
            utcTime: String,
            inputFormat: String = "yyyy-MM-dd'T'HH:mm:ssZ",
            outputFormat: String = "yyyy-MM-dd HH:mm:ss",
        ): String {
            return try {
                // Formatter to parse the UTC input time
                val utcFormatter =
                    DateTimeFormatter.ofPattern(inputFormat).withZone(ZoneId.of("UTC"))

                // Parse the input string to an Instant
                val instant = Instant.from(utcFormatter.parse(utcTime))

                // Formatter to convert Instant to local time
                val localFormatter =
                    DateTimeFormatter.ofPattern(outputFormat).withZone(ZoneId.systemDefault())

                // Format the Instant to local time
                localFormatter.format(instant)
            } catch (e: Exception) {
                Log.e("ConvertTime", "Error converting UTC to local time", e)
                "Invalid time format"
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

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ride_item, parent, false)
        return RideViewHolder(view)
    }

    override fun onBindViewHolder(holder: RideViewHolder, position: Int) {
        val ride = rides[position]
        holder.bind(ride) // Bind data to the ViewHolder

        holder.itemView.setOnClickListener {
            // Use the `ride` object to pass data to the BottomSheet
            val rideDetails = DataX(
                available_seats = ride.data.available_seats,
                created_at = ride.data.created_at,
                driver = ride.data.driver,
                driver_name = ride.data.driver_name,
                driver_rating = ride.data.driver_rating,
                start_location = ride.data.start_location,
                end_location = ride.data.end_location,
                end_point = ride.data.end_point,
                id = ride.data.id,
                start_point = ride.data.start_point,
                start_time = convertUtcToLocalTime(
                    ride.data.start_time, "yyyy-MM-dd'T'HH:mm:ss'Z'"
                ),
                status = ride.data.status,
                car_details = CarDetails(
                    car_modal = ride.data.car_details.car_modal,
                    car_number = ride.data.car_details.car_number
                ),
                driver_profile_photo = ride.data.driver_profile_photo
            )
            val bottomSheet = RideDetailsBottomSheetFragment.newInstance(rideDetails,bundle)
            bottomSheet.show(
                (holder.itemView.context as FragmentActivity).supportFragmentManager,
                bottomSheet.tag
            )
        }
    }

    private fun convertUtcToLocalTime(
        utcTime: String,
        inputFormat: String = "yyyy-MM-dd'T'HH:mm:ssZ",
        outputFormat: String = "yyyy-MM-dd HH:mm:ss",
    ): String {
        return try {
            // Formatter to parse the UTC input time
            val utcFormatter =
                DateTimeFormatter.ofPattern(inputFormat).withZone(ZoneId.of("UTC"))
            val instant = Instant.from(utcFormatter.parse(utcTime))
            val localFormatter =
                DateTimeFormatter.ofPattern(outputFormat).withZone(ZoneId.systemDefault())
            localFormatter.format(instant)
        } catch (e: Exception) {
            Log.e("ConvertTime", "Error converting UTC to local time", e)
            "Invalid time format"
        }
    }


    override fun getItemCount() = rides.size
    fun updateRides(newRides: List<Data>) {
        this.rides = newRides
        notifyDataSetChanged()
    }

    fun clearRides() {
        this.rides = emptyList()
        notifyDataSetChanged()
    }

}

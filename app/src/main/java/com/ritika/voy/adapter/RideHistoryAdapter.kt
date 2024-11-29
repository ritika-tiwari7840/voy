package com.ritika.voy.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ritika.voy.R
import com.ritika.voy.api.dataclasses.RideDetails
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class RideHistoryAdapter(private val rides: List<RideDetails>) :
    RecyclerView.Adapter<RideHistoryAdapter.RideViewHolder>() {

    inner class RideViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val startLocation: TextView = view.findViewById(R.id.start_location)
        val endLocation: TextView = view.findViewById(R.id.end_location)
        val time: TextView = view.findViewById(R.id.time)
        val date: TextView = view.findViewById(R.id.date)
        val status: TextView = view.findViewById(R.id.status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ride_history, parent, false)
        return RideViewHolder(view)
    }

    override fun onBindViewHolder(holder: RideViewHolder, position: Int) {
        val ride = rides[position]
        val isoDate = "${ride.start_time}"
        val (date, time) = formatDateTime(isoDate)
        holder.startLocation.text = ride.start_location
        holder.endLocation.text = ride.end_location
        holder.time.text = time
        holder.date.text = date
        holder.status.text = ride.status
    }

    override fun getItemCount(): Int = rides.size
}

fun formatDateTime(isoDate: String): Pair<String, String> {
    // Define the input format (ISO 8601)
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    inputFormat.timeZone = TimeZone.getTimeZone("UTC") // Parse the date as UTC

    // Define the output formats
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) // Date, Month, Year
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())       // Time in 12-hour format

    // Parse the ISO date
    val date = inputFormat.parse(isoDate)

    // Format the date and time
    val formattedDate = dateFormat.format(date)
    val formattedTime = timeFormat.format(date)

    return Pair(formattedDate, formattedTime)
}

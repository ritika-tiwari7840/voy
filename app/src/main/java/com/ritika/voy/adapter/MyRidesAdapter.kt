package com.ritika.voy.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ritika.voy.R
import com.ritika.voy.api.dataclasses.MyRideItem

class MyRidesAdapter(private var rideList: MutableList<MyRideItem>) :
    RecyclerView.Adapter<MyRidesAdapter.RideViewHolder>() {

    // Method to update the ride list data dynamically
    fun updateRides(newRides: List<MyRideItem>) {
        rideList.clear()
        rideList.addAll(newRides)
        notifyDataSetChanged()  // Notify the adapter that the data has changed
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_ride_item, parent, false)
        return RideViewHolder(view)
    }

    override fun onBindViewHolder(holder: RideViewHolder, position: Int) {
        val ride = rideList[position]
        holder.bind(ride)
    }

    override fun getItemCount() = rideList.size

    // ViewHolder class to bind each ride item to its views
    class RideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val startLocationText: TextView = itemView.findViewById(R.id.start_location)
        private val endLocationText: TextView = itemView.findViewById(R.id.end_location)
        private val startTimeText: TextView = itemView.findViewById(R.id.date_time_picker)
        private val statusText: TextView = itemView.findViewById(R.id.status)

        fun bind(ride: MyRideItem) {
            startLocationText.text = ride.startLocation
            endLocationText.text = ride.endLocation
            startTimeText.text = ride.startTime
            statusText.text = ride.status
        }
    }
}

package com.ritika.voy.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ritika.voy.R
import com.ritika.voy.api.dataclasses.DataXXXX

class MatchingRidesAdapter(private var rides: List<DataXXXX>) :
    RecyclerView.Adapter<MatchingRidesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rideId: TextView = view.findViewById(R.id.rideId)
        val passengerName: TextView = view.findViewById(R.id.passengerName)
        val pickupLocation: TextView = view.findViewById(R.id.pickupLocation)
        val dropoffLocation: TextView = view.findViewById(R.id.dropoffLocation)
        val seatsNeeded: TextView = view.findViewById(R.id.seatsNeeded)
        val status: TextView = view.findViewById(R.id.status)
        val createdAt: TextView = view.findViewById(R.id.createdAt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_matching_ride, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ride = rides[position].data
        holder.rideId.text = "Ride ID: ${ride.id}"
        holder.passengerName.text = "Passenger: ${ride.passenger_name}"
        holder.pickupLocation.text = "From: ${ride.pickup_location}"
        holder.dropoffLocation.text = "To: ${ride.dropoff_location}"
        holder.seatsNeeded.text = "Seats needed: ${ride.seats_needed}"
        holder.status.text = "Status: ${ride.status}"
        holder.createdAt.text = "Created at: ${ride.created_at}"
    }

    override fun getItemCount() = rides.size

    fun updateRides(newRides: List<DataXXXX>) {
        rides = newRides
        notifyDataSetChanged()
    }
}
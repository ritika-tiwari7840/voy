package com.ritika.voy.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ritika.voy.R
import com.ritika.voy.api.dataclasses.MyRideItem
import java.text.SimpleDateFormat
import java.util.Locale

class MyRidesAdapter(
    private val onItemClickListener: OnItemClickListener? = null
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
        private val onItemClickListener: OnItemClickListener?
    ) : RecyclerView.ViewHolder(itemView) {
        private val startLocationText: TextView = itemView.findViewById(R.id.start_location)
        private val endLocationText: TextView = itemView.findViewById(R.id.end_location)
        private val startTimeText: TextView = itemView.findViewById(R.id.date_time_picker)
        private val statusText: TextView = itemView.findViewById(R.id.status)

        private var currentRide: MyRideItem? = null

        init {
            itemView.setOnClickListener {
                currentRide?.let { ride ->
                    // Call the interface method if set
                    onItemClickListener?.onItemClick(ride)
                }
            }
        }

        fun bind(ride: MyRideItem) {
            currentRide = ride
            startLocationText.text = ride.startLocation
            endLocationText.text = ride.endLocation

            startTimeText.text = formatDateTime(ride.startTime)
            statusText.text = ride.status
        }

        private fun formatDateTime(dateTime: String?): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                val date = inputFormat.parse(dateTime ?: return "")
                outputFormat.format(date ?: return "")
            } catch (e: Exception) {
                dateTime ?: ""
            }
        }
    }

    // DiffUtil callback to efficiently update the list
    private class RideDiffCallback : DiffUtil.ItemCallback<MyRideItem>() {
        override fun areItemsTheSame(oldItem: MyRideItem, newItem: MyRideItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MyRideItem, newItem: MyRideItem): Boolean {
            return oldItem == newItem
        }
    }
}
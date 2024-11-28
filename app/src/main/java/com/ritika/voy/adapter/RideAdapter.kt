// RideAdapter.kt
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ritika.voy.R
import com.ritika.voy.api.dataclasses.Data

class RideAdapter(private val rides: List<Data>) :
    RecyclerView.Adapter<RideAdapter.RideViewHolder>() {

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
        holder.bind(rides[position])
    }

    override fun getItemCount() = rides.size
}
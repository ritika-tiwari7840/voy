import android.os.Parcelable
import com.ritika.voy.api.dataclasses.EndPoint
import com.ritika.voy.api.dataclasses.StartPoint
import kotlinx.parcelize.Parcelize

@Parcelize
data class DataX(
    val available_seats: Int,
    val created_at: String,
    val driver: Int,
    val driver_name: String,
    val end_location: String,
    val end_point: EndPoint,
    val id: Int,
//    val route_line: Any, // You may need to make `route_line` Parcelable or replace it with a Parcelable type
    val start_location: String,
    val start_point: StartPoint,
    val start_time: String,
    val status: String,
    val car_details: CarDetails,
    val driver_profile_photo: String,
    val driver_rating: Double,
) : Parcelable

@Parcelize
data class CarDetails(
    val car_modal: String? = "",
    val car_number: String? = "",
) : Parcelable

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationItem(
    val latitude: Double,
    val longitude: Double,
    val address: String
) : Parcelable

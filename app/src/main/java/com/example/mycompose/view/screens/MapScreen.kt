import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.mycompose.GeocodingResponse
import com.example.mycompose.GeocodingService
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun MapScreen(navController: NavHostController, onLocationSelected: (String) -> Unit) {
    val sydney = LatLng(-34.0, 151.0) // Initial location
    val cameraPositionState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(sydney, 10f)
    }

    val markerPositionState = remember { mutableStateOf(sydney) }
    val addressState = remember { mutableStateOf("") }

    val retrofit = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val geocodingService = retrofit.create(GeocodingService::class.java)

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = { latLng ->
            markerPositionState.value = latLng
            val latLngString = "${latLng.latitude},${latLng.longitude}"

            // Geocoding API request
            val call = geocodingService.getAddress(latLngString, "AIzaSyB7giJpXXt25u0Ald-xIccjGfYUpGoNhHo")
            Log.d("GeocodingRequest", "Request URL: ${call.request().url}")

            call.enqueue(object : retrofit2.Callback<GeocodingResponse> {
                override fun onResponse(
                    call: Call<GeocodingResponse>,
                    response: retrofit2.Response<GeocodingResponse>
                ) {
                    if (response.isSuccessful) {
                        val address = response.body()?.results?.firstOrNull()?.formatted_address
                        addressState.value = address ?: "Address not found"
                        onLocationSelected(addressState.value)
                    } else {
                        addressState.value = "Failed to get address"
                        onLocationSelected(addressState.value)
                    }

                    // Save the location and navigate back
                    navController.previousBackStackEntry?.savedStateHandle?.set("location", addressState.value)
                }

                override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) {
                    addressState.value = "Failed to get address"
                    onLocationSelected(addressState.value)
                }
            })

            // Save the location and navigate back
            navController.previousBackStackEntry?.savedStateHandle?.set("location", addressState.value)
            navController.popBackStack()
        }
    ) {
        Marker(
            state = MarkerState(position = markerPositionState.value),
            title = addressState.value
        )
    }
}


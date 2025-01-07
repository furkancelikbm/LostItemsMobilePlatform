import android.content.Context
import android.content.Intent
import com.google.android.gms.maps.model.LatLng
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycompose.model.SuggestionNew
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.model.Place
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MapViewModel : ViewModel() {

    // UI States
    private val _userLocation = mutableStateOf<LatLng?>(null)
    val userLocation: State<LatLng?> = _userLocation

    private val _locationSuggestions = mutableStateOf<List<SuggestionNew>>(emptyList())
    val locationSuggestions: State<List<SuggestionNew>> = _locationSuggestions

    private val _placeName = mutableStateOf("")
    val placeName: State<String> = _placeName

    private val _errorState = mutableStateOf<String?>(null)
    val errorState: State<String?> = _errorState

    // Fetch user location
    fun fetchUserLocation(context: Context, fusedLocationClient: FusedLocationProviderClient) {
        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    location?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        _userLocation.value = latLng
                        fetchPlaceName(context, latLng)
                    } ?: run {
                        _errorState.value = "Location is null"
                    }
                }
                .addOnFailureListener { exception ->
                    _errorState.value = "Failed to get current location: ${exception.localizedMessage}"
                }
        } else {
            _errorState.value = "Permission not granted"
        }
    }

    // Convert latitude and longitude to place name
    private fun fetchPlaceName(context: Context, latLng: LatLng) {
        viewModelScope.launch {
            val placeName = getPlaceNameFromLatLng(context, latLng)
            _placeName.value = placeName ?: "Unknown Location"
        }
    }

    // Add this public method to your MapViewModel
    fun getPlaceNameForLatLng(context: Context, latLng: LatLng) {
        fetchPlaceName(context, latLng) // Calls the private method internally
    }

    private suspend fun getPlaceNameFromLatLng(context: Context, latLng: LatLng): String? {
        return try {
            val geocoder = Geocoder(context)
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            addresses?.firstOrNull()?.getAddressLine(0)
        } catch (e: Exception) {
            _errorState.value = "Error fetching address: ${e.localizedMessage}"
            null
        }
    }

    // Fetch location suggestions based on the query
    fun fetchLocationSuggestions(query: String, context: Context) {
        if (!Places.isInitialized()) {
            Places.initialize(context, "AIzaSyB7giJpXXt25u0Ald-xIccjGfYUpGoNhHo")
        }

        val placesClient = Places.createClient(context)
        val request = FindAutocompletePredictionsRequest.builder().setQuery(query).build()

        viewModelScope.launch {
            try {
                val response = placesClient.findAutocompletePredictions(request).await()
                val suggestions = response.autocompletePredictions.map { prediction ->
                    // Fetch place details to get latLng for each suggestion
                    val placeId = prediction.placeId
                    val placeDetails = fetchPlaceDetailsById(placeId, context)
                    SuggestionNew(placeId, prediction.getFullText(null).toString(), placeDetails?.latLng)
                }
                _locationSuggestions.value = suggestions
            } catch (e: Exception) {
                _errorState.value = "Error fetching suggestions: ${e.localizedMessage}"
            }
        }
    }

    // Fetch place details for a given placeId
    private suspend fun fetchPlaceDetailsById(placeId: String, context: Context): Place? {
        return try {
            val placesClient = Places.createClient(context)
            val placeFields = listOf(Place.Field.LAT_LNG)
            val request = FetchPlaceRequest.builder(placeId, placeFields).build()
            val response = placesClient.fetchPlace(request).await()
            response.place
        } catch (e: Exception) {
            _errorState.value = "Error fetching place details: ${e.localizedMessage}"
            null
        }
    }

    // Select a suggested location and update the user location with full address
    fun selectSuggestedLocation(suggestion: SuggestionNew, context: Context) {
        viewModelScope.launch {
            try {
                // Fetch place details using placeId to get LatLng and full address
                val placesClient = Places.createClient(context)
                val placeFields = listOf(Place.Field.LAT_LNG, Place.Field.NAME, Place.Field.ADDRESS)
                val request = FetchPlaceRequest.builder(suggestion.placeId, placeFields).build()

                val response = placesClient.fetchPlace(request).await()
                val place = response.place
                place.latLng?.let { latLng ->
                    // Update user location with fetched LatLng
                    _userLocation.value = latLng
                    // Set the full address in placeName
                    _placeName.value = place.address ?: "Unknown Location"
                } ?: run {
                    _errorState.value = "Location not found for placeId: ${suggestion.placeId}"
                }
            } catch (e: Exception) {
                _errorState.value = "Error fetching place details: ${e.localizedMessage}"
            }
        }
    }


    // Check GPS and fetch location
    fun checkGpsAndFetchLocation(context: Context, fusedLocationClient: FusedLocationProviderClient, onLocationFetched: (String) -> Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (isGpsEnabled) {
            fetchUserLocation(context, fusedLocationClient)
        } else {
            _errorState.value = "GPS is disabled. Please enable it to fetch location."
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            context.startActivity(intent)
        }
    }
}

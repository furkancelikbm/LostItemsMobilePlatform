package com.example.mycompose.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import androidx.lifecycle.viewModelScope
import com.example.mycompose.model.Suggestion
import com.google.android.gms.location.LocationRequest
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.model.Place
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MapViewModel : ViewModel() {

    private val _userLocation = mutableStateOf<LatLng?>(null)
    val userLocation: State<LatLng?> = _userLocation

    private val _locationSuggestions = mutableStateOf<List<Suggestion>>(emptyList())
    val locationSuggestions: State<List<Suggestion>> = _locationSuggestions

    // Camera position state
    private val _cameraPositionState = mutableStateOf(CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 15f))
    val cameraPositionState: State<CameraPosition> = _cameraPositionState

    private val _placeName = mutableStateOf("")
    val placeName: State<String> = _placeName


    fun fetchUserLocation(
        context: Context,
        fusedLocationClient: FusedLocationProviderClient,
        cameraPositionState: CameraPositionState
    ) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    location?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        _userLocation.value = latLng

                        // Update the camera position after getting the current location
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)

                        // Convert the latitude and longitude to a place name using Geocoder
                        try {
                            val geocoder = Geocoder(context)
                            val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                            if (addresses!!.isNotEmpty()) {  // Fixed the typo here
                                val address = addresses[0]
                                val placeName = address.getAddressLine(0)
                                _placeName.value = placeName // Update place name
                            } else {
                                Log.e("MapViewModel", "No address found for the location")
                            }
                        } catch (e: Exception) {
                            Log.e("MapViewModel", "Error getting address: ${e.localizedMessage}")
                        }
                    } ?: run {
                        Log.e("MapViewModel", "Location is null")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("MapViewModel", "Failed to get current location", exception)
                }
        } else {
            Log.e("MapViewModel", "Permission not granted")
        }
    }



    fun fetchLocationSuggestions(query: String, context: Context) {
        if (!Places.isInitialized()) {
            Places.initialize(context, "AIzaSyB7giJpXXt25u0Ald-xIccjGfYUpGoNhHo") // Replace with your actual API key
        }
        val placesClient = Places.createClient(context)
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        viewModelScope.launch {
            try {
                val response = placesClient.findAutocompletePredictions(request).await()
                val suggestions = response.autocompletePredictions.map {
                    Suggestion(it.placeId, it.getFullText(null).toString()) // Populate description with full text
                }
                _locationSuggestions.value = suggestions
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error fetching suggestions: ${e.localizedMessage}")
            }
        }
    }


    fun selectSuggestedLocation(suggestion: Suggestion, cameraPositionState: CameraPositionState, context: Context) {
        Log.d("MapViewModel", "Selected Suggestion: ${suggestion.description}") // Log the full suggestion text
        fetchPlaceDetails(suggestion.placeId, cameraPositionState, context)
    }

    private fun fetchPlaceDetails(placeId: String, cameraPositionState: CameraPositionState, context: Context) {
        val placesClient = Places.createClient(context)

        // Include fields for name, location, and description (address)
        val placeFields = listOf(Place.Field.LAT_LNG, Place.Field.NAME, Place.Field.ADDRESS)
        val request = FetchPlaceRequest.builder(placeId, placeFields).build()

        viewModelScope.launch {
            try {
                val response = placesClient.fetchPlace(request).await()
                val place = response.place
                place.latLng?.let { latLng ->
                    _userLocation.value = latLng
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)

                    // Log place details
                    Log.d("MapViewModel", "Place Name: ${place.name}")
                    Log.d("MapViewModel", "Latitude: ${latLng.latitude}")
                    Log.d("MapViewModel", "Longitude: ${latLng.longitude}")
                    Log.d("MapViewModel", "Description: ${place.address}")
                } ?: run {
                    Log.e("MapViewModel", "Location not found for placeId: $placeId")
                }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error fetching place details: ${e.localizedMessage}")
            }
        }
    }

}
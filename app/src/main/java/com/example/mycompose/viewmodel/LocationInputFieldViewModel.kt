package com.example.mycompose.viewmodel

import android.util.Log
import com.example.mycompose.model.PlacesApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycompose.model.Place
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LocationInputFieldViewModel : ViewModel() {

    var pickUp by mutableStateOf(TextFieldValue(text = ""))
        private set
    var unSelectedLocationId by mutableStateOf("")
        private set
    var selectedPlace by mutableStateOf<Place?>(null)
        private set

    private val placesApi = PlacesApi()

    // This will fetch places every time pickUp is changed and return them as state
    val pickupLocationPlaces: StateFlow<List<Place>> =
        snapshotFlow { pickUp }
            .mapLatest {
                placesApi.fetchPlaces(
                    key = "AIzaSyB7giJpXXt25u0Ald-xIccjGfYUpGoNhHo",  // Replace with your actual API key
                    input = pickUp.text
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    // Function to fetch detailed place information for a selected place
    suspend fun fetchPlaceDetailsForPlace(placeId: String): Place? {
        return placesApi.fetchPlaceDetailsForPlace(
            placeId = placeId)
    }

    fun onPickUpValueChanged(value: TextFieldValue) {
        pickUp = value
    }

    fun onPlaceClick(value: String) {
        pickUp = TextFieldValue(
            text = value,
            selection = TextRange(value.length)
        )
        // Seçilen yerin ID'sini al
        val place = pickupLocationPlaces.value.find { it.name == value }
        unSelectedLocationId = place?.id ?: "" // Seçilen yerin ID'sini kaydet
        // Fetch detailed information about the selected place
        viewModelScope.launch {
            unSelectedLocationId.takeIf { it.isNotEmpty() }?.let { placeId ->
                val placeDetails = fetchPlaceDetailsForPlace(placeId)
                placeDetails?.let {
                    selectedPlace = it
                    Log.d(
                        "LocationInputFieldViewModel",
                        "Selected Place Details: Name=${it.name}, Latitude=${it.latitude}, Longitude=${it.longitude}"
                    )
                } ?: run {
                    Log.e("LocationInputFieldViewModel", "Failed to fetch place details for Place ID: $placeId")
                }
            }
        }
    }

    fun checkAndSelectFirstPlace() {
        // Eğer konum seçilmediyse, ilk öğeyi otomatik olarak seç
        if (pickupLocationPlaces.value.isNotEmpty()) {
            val firstPlace = pickupLocationPlaces.value.first()
            onPlaceClick(firstPlace.name)
        } else {
            unSelectedLocationId = "0"
        }
    }
}

package com.example.mycompose

import PlacesApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

class RideViewModel : ViewModel() {

    var pickUp by mutableStateOf(TextFieldValue(text = ""))
        private set

    private val placesApi = PlacesApi()

    // This will fetch places every time pickUp is changed and return them as state
    val pickupLocationPlaces: StateFlow<List<Place>> =
        snapshotFlow { pickUp }
            .mapLatest {
                placesApi.fetchPlaces(
                    key = "AIzaSyB7giJpXXt25u0Ald-xIccjGfYUpGoNhHo",
                    input = pickUp.text
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun onPickUpValueChanged(value: TextFieldValue) {
        pickUp = value
    }

    fun onPlaceClick(value: String) {
        pickUp = TextFieldValue(
            text = value,
            selection = TextRange(value.length)
        )
    }
}
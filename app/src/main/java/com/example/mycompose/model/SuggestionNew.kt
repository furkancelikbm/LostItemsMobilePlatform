package com.example.mycompose.model

import com.google.android.gms.maps.model.LatLng

data class SuggestionNew(
    val placeId: String,
    val fullText: String,
    val latLng: LatLng? // Add LatLng here
)

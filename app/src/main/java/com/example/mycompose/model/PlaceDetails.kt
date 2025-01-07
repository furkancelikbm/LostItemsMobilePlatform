package com.example.mycompose.model

import com.google.type.LatLng

data class PlaceDetails(
    val latLng: LatLng?,
    val name: String?,
    val address: String?
)

package com.example.mycompose.model

data class Place(
    val id: String,  // Add the placeId field
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val formattedAddress: String
)

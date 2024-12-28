package com.example.mycompose.model

import kotlinx.serialization.Serializable

@Serializable
data class PlaceDetailsDto(
    val result: PlaceDetailsResult? = null
)

@Serializable
data class PlaceDetailsResult(
    val name: String = "",
    val formatted_address: String = "",
    val geometry: Geometry = Geometry()
)

@Serializable
data class Geometry(
    val location: Location = Location(),
    val viewport: Viewport = Viewport() // Add viewport here
)

@Serializable
data class Location(
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

@Serializable
data class Viewport(
    val northeast: Location = Location(), // Viewport's northeast corner
    val southwest: Location = Location()  // Viewport's southwest corner
)

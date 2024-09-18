package com.example.mycompose


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlacesDto(
    @SerialName("predictions")
    val predictions: List<Prediction>,
    @SerialName("status")
    val status: String
)
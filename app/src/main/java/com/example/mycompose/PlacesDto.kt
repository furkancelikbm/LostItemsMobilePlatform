package com.example.mycompose


import kotlinx.serialization.Serializable

@Serializable
data class PlacesDto(
    val predictions: List<Prediction> = listOf(),
    val status: String = ""
) {
    fun toPlacesList() = predictions.map { it.toPlace() }
}
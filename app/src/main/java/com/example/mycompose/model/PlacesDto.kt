package com.example.mycompose.model


import kotlinx.serialization.Serializable

@Serializable
data class PlacesDto(
    val predictions: List<Prediction> = listOf(),
    val status: String = ""
) {
    fun toPlacesList() = predictions.map { it.toPlace() }
}
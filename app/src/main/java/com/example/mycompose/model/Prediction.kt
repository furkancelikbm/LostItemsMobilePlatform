package com.example.mycompose.model

import kotlinx.serialization.Serializable


@Serializable
data class Prediction(
    val description: String = "",
    val matched_substrings: List<MatchedSubstring> = listOf(),
    val place_id: String = "",
    val reference: String = "",
    val structured_formatting: StructuredFormatting = StructuredFormatting(),
    val terms: List<Term> = listOf(),
    val types: List<String> = listOf(),
) {
    fun toPlace(formattedAddress: String = ""): Place {
        return Place(
            id = place_id,
            name = description,
            formattedAddress = formattedAddress, // Pass the formattedAddress here
            latitude = 0.0, // Default value
            longitude = 0.0 // Default value
        )
    }
}



package com.example.mycompose


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Prediction(
    @SerialName("description")
    val description: String,
    @SerialName("matched_substrings")
    val matchedSubstrings: List<MatchedSubstring>,
    @SerialName("place_id")
    val placeId: String,
    @SerialName("reference")
    val reference: String,
    @SerialName("structured_formatting")
    val structuredFormatting: StructuredFormatting,
    @SerialName("terms")
    val terms: List<Term>,
    @SerialName("types")
    val types: List<String>
)
package com.example.mycompose

import kotlinx.serialization.Serializable


@Suppress("PLUGIN_IS_NOT_ENABLED")
@Serializable
data class Prediction(
    val description: String = "",
    val matched_substrings: List<MatchedSubstring> = listOf(),
    val place_id: String = "",
    val reference: String = "",
    val structured_formatting: StructuredFormatting = StructuredFormatting(),
    val terms: List<Term> = listOf(),
    val types: List<String> = listOf()
) {
    fun toPlace() = Place(
        id = place_id,
        name = description
    )
}
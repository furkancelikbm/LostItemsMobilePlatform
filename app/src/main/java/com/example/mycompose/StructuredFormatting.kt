package com.example.mycompose

import kotlinx.serialization.Serializable

@Serializable
data class StructuredFormatting(
    val main_text: String = "",
    val main_text_matched_substrings: List<MatchedSubstring> = listOf(),
    val secondary_text: String = "",
    val secondary_text_matched_substrings: List<MatchedSubstring> = listOf() // Include this field
)


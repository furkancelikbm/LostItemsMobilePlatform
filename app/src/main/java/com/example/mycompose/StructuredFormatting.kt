package com.example.mycompose


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StructuredFormatting(
    @SerialName("main_text")
    val mainText: String,
    @SerialName("main_text_matched_substrings")
    val mainTextMatchedSubstrings: List<MainTextMatchedSubstring>,
    @SerialName("secondary_text")
    val secondaryText: String
)
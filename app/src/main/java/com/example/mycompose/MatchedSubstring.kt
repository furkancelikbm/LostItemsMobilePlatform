package com.example.mycompose


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MatchedSubstring(
    @SerialName("length")
    val length: Int,
    @SerialName("offset")
    val offset: Int
)
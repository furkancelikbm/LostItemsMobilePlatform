package com.example.mycompose


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MainTextMatchedSubstring(
    @SerialName("length")
    val length: Int,
    @SerialName("offset")
    val offset: Int
)
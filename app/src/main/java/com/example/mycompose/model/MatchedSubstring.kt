package com.example.mycompose.model


import kotlinx.serialization.Serializable

@Serializable
data class MatchedSubstring(
    val length: Int = 0,
    val offset: Int = 0
)
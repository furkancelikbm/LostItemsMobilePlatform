package com.example.mycompose


import kotlinx.serialization.Serializable

@Serializable
data class MatchedSubstring(
    val length: Int = 0,
    val offset: Int = 0
)
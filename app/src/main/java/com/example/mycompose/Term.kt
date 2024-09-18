package com.example.mycompose


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Term(
    @SerialName("offset")
    val offset: Int,
    @SerialName("value")
    val value: String
)
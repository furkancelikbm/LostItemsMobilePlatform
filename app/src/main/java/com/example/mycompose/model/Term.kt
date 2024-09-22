package com.example.mycompose.model


import kotlinx.serialization.Serializable

@Serializable
data class Term(
    val value: String = "",
    val offset: Int = 0
)
package com.example.mycompose.model

import LocationItem

data class AdModel(
    val id: String,
    val title: String,
    val description: String,
    val location: LocationItem,
    val imageUrls: List<String>,
    val userId: String // Kullanıcı ID'si
)

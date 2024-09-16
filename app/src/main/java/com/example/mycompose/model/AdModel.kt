package com.example.mycompose.model

data class AdModel(
    val id: String,
    val title: String,
    val description: String,
    val location: String,
    val imageUrls: List<String>,
    val userId: String // Kullanıcı ID'si
)

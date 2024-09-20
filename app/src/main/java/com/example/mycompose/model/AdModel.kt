package com.example.mycompose.model

import com.google.firebase.Timestamp
import java.time.LocalDateTime

data class AdModel(
    val id: String,
    val title: String,
    val description: String,
    val location: String,
    val imageUrls: List<String>,
    val userId: String, // Kullanıcı ID'si
    val locationId:String,
    val timestamp:LocalDateTime,
)

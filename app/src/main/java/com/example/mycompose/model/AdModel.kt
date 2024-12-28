package com.example.mycompose.model

data class AdModel(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val imageUrls: List<String> = listOf(),
    val userId: String = "", // Kullanıcı ID'si
    val locationId: String = "",
    val timestamp: Long = System.currentTimeMillis() ,// Store as Long (epoch time)
    val category:String="",
    val adDate: String = "",
    val latitude: Double? = null,  // Keep it nullable
    val longitude: Double? = null  // Keep it nullable

)

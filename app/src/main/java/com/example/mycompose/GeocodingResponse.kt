// GeocodingResponse.kt
package com.example.mycompose

data class GeocodingResponse(
    val results: List<Result>,
    val status: String
)

data class Result(
    val formatted_address: String
)

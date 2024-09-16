// GeocodingService.kt
package com.example.mycompose

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingService {
    @GET("maps/api/geocode/json")
    fun getAddress(
        @Query("latlng") latLng: String,
        @Query("key") apiKey: String
    ): Call<GeocodingResponse>
}

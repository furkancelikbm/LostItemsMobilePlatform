package com.example.mycompose.model

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.Json
import java.io.IOException

class PlacesApi {

    private val client: HttpClient = HttpClient {
        install(Logging) {
            level = LogLevel.HEADERS
            logger = object : io.ktor.client.plugins.logging.Logger {
                override fun log(message: String) {
                    Napier.v(tag = "com.example.mycompose.model.PlacesApi", message = message)
                }
            }
        }

        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true  // Ensures that unknown keys are ignored
                    prettyPrint = true
                    isLenient = true
                }
            )
        }
    }

    suspend fun fetchPlaces(
        key: String,
        input: String
    ): List<Place> {
        return try {
            val response = client.get {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "maps.googleapis.com"
                    path("maps/api/place/autocomplete/json")

                    parameters.append("key", key)
                    parameters.append("types", "address")
                    parameters.append("input", input)
                }
            }

            // Convert the response to PlacesDto object
            val placesDto = Json.decodeFromString<PlacesDto>(response.bodyAsText())

            // Return the places list
            placesDto.toPlacesList()

        } catch (e: IOException) {
            Napier.e("Network error: ${e.message}", e)
            emptyList() // Return an empty list on error
        } catch (e: Exception) {
            Napier.e("Unexpected error: ${e.message}", e)
            emptyList() // Return an empty list on error
        }
    }

    suspend fun fetchPlaceDetailsForPlace(placeId: String): Place? {
        return try {
            val response = client.get {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "maps.googleapis.com"
                    path("maps/api/place/details/json")

                    parameters.append("key", "AIzaSyB7giJpXXt25u0Ald-xIccjGfYUpGoNhHo")  // Gerçek API anahtarınız
                    parameters.append("placeid", placeId)
                }
            }

            // Yanıtı log'la
            val responseBody = response.bodyAsText()
//            Log.d("MainActivity", "API Response: $responseBody")

            // JSON çözümleyicisini, bilinmeyen anahtarları yoksayacak şekilde yapılandır
            val json = Json { ignoreUnknownKeys = true }

            // Yanıtı PlaceDetailsDto'ya çevir
            val placeDetailsDto = json.decodeFromString<PlaceDetailsDto>(responseBody)

            // Geometry.location'dan lat ve lng değerlerini al
            placeDetailsDto.result?.let {
                val latitude = it.geometry.location?.lat  // Yer koordinatının enlemi
                val longitude = it.geometry.location?.lng  // Yer koordinatının boylamı

                // Koordinatları log'la
                Log.d("PlaceDetails", "Latitude: $latitude, Longitude: $longitude")

                // Place nesnesini oluştur ve döndür
                Place(
                    id = placeId,
                    name = it.name,
                    latitude = latitude ?: 0.0,  // Eğer null ise 0.0 olarak ayarlanır
                    longitude = longitude ?: 0.0,  // Eğer null ise 0.0 olarak ayarlanır
                    formattedAddress = it.formatted_address
                )
            }

        } catch (e: Exception) {
            // Hata durumunda log'la
            Log.d("Error", "Error fetching place details: ${e.message}", e)
            null // Hata durumunda null döner
        }
    }

}

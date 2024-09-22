package com.example.mycompose.model

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
                    ignoreUnknownKeys = true
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

    // You may need a way to close the client when the API instance is no longer needed
    fun closeClient() {
        client.close()
    }
}

import com.example.mycompose.Place
import com.example.mycompose.PlacesDto
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.github.aakira.napier.Napier

class PlacesApi {
    private val client: HttpClient = HttpClient {
        install(Logging) {
            level = LogLevel.HEADERS
            logger = object : io.ktor.client.plugins.logging.Logger {
                override fun log(message: String) {
                    Napier.v(tag = "PlacesApi", message = message)
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

        // This converts our JSON to a PlacesDto object
        val placesDto = Json.decodeFromString<PlacesDto>(response.bodyAsText())

        // We return the places list
        return placesDto.toPlacesList()
    }
}

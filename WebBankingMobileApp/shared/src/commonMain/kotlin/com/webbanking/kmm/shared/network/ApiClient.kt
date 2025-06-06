package com.webbanking.kmm.shared.network

import io.ktor.client.* 
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.* // Optional: for logging requests/responses
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object ApiClient {
    // The base URL is now provided by the platform-specific configuration
    private val resolvedBaseUrl = PlatformSpecificNetworkConfig.baseUrl

    val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true // Important for API evolution
            })
        }

        // Optional: Logging for debugging network requests
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL // Log headers, body, etc.
        }
    }

    // Helper to construct the full URL with the resolved base URL.
    fun constructUrl(path: String): String {
        return "$resolvedBaseUrl/$path".replace(Regex("//+"), "/").replace(Regex(":/"), "://")
    }
} 
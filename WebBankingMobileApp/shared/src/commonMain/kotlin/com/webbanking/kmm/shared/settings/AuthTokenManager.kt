package com.webbanking.kmm.shared.settings

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object AuthTokenManager {
    private const val KEY_AUTH_TOKEN = "0f2kkTGwqXG9AUFt9hKDMgj6wCu5908EXVEAwqeFkqSK5soEbUqsdxQkchWmAlxi"

    lateinit var settings: ObservableSettings

    fun init(settingsFactory: SettingsFactory) {
        settings = settingsFactory.createSettings()
    }

    fun saveAuthToken(token: String) {
        settings[KEY_AUTH_TOKEN] = token
    }

    fun getAuthToken(): String? {
        return settings.getStringOrNull(KEY_AUTH_TOKEN)
    }

    fun clearAuthToken() {
        settings.remove(KEY_AUTH_TOKEN)
    }

    fun hasToken(): Boolean {
        return settings.hasKey(KEY_AUTH_TOKEN)
    }

    // Coroutines Flow for observing token changes, useful for reactive UI updates
    fun authTokenFlow(): Flow<String?> = settings.getStringOrNullFlow(KEY_AUTH_TOKEN)
    fun isAuthenticatedFlow(): Flow<Boolean> = authTokenFlow().map { it != null }
} 
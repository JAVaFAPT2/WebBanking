package com.webbanking.kmm.shared.settings

import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.getStringFlow
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object AuthTokenManager {
    private const val KEY_AUTH_TOKEN = "auth_token"

    private val settings: Settings by lazy {
        SettingsFactory().createSettings()
    }

    fun saveAuthToken(token: String) {
        settings[KEY_AUTH_TOKEN] = token
    }

    fun getAuthToken(): String? {
        return settings[KEY_AUTH_TOKEN]
    }

    fun clearAuthToken() {
        settings.remove(KEY_AUTH_TOKEN)
    }

    fun hasToken(): Boolean {
        return settings.hasKey(KEY_AUTH_TOKEN)
    }

    // Coroutines Flow for observing token changes, useful for reactive UI updates
    val authTokenFlow: Flow<String?> = settings.getStringOrNullFlow(KEY_AUTH_TOKEN)

    val isAuthenticatedFlow: Flow<Boolean> = authTokenFlow.map { it != null }
} 
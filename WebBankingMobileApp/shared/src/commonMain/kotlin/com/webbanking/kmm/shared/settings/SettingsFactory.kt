package com.webbanking.kmm.shared.settings

import com.russhwolf.settings.Settings

expect class SettingsFactory {
    fun createSettings(): Settings
} 
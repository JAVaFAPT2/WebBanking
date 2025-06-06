package com.webbanking.kmm.shared.settings

import com.russhwolf.settings.ObservableSettings

expect class SettingsFactory() {
    fun createSettings(): ObservableSettings
} 
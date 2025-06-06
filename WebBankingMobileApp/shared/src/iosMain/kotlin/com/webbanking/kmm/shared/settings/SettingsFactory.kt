package com.webbanking.kmm.shared.settings

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults
import com.russhwolf.settings.ObservableSettings

actual class SettingsFactory actual constructor() {
    actual fun createSettings(): ObservableSettings {
        // For iOS, NSUserDefaults is the standard place for simple key-value storage.
        // You can use a specific suite name if needed, or default to standardUserDefaults.
        val userDefaults = NSUserDefaults.standardUserDefaults
        return NSUserDefaultsSettings(userDefaults)
    }
} 
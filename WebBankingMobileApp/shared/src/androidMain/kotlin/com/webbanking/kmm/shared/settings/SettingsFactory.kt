package com.webbanking.kmm.shared.settings

import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import com.russhwolf.settings.ObservableSettings
import android.content.Context

// Removed unused imports (android.app.Application, android.content.Context) and comments.

object AndroidContextHolder {
    var context: android.content.Context? = null
}

actual class SettingsFactory actual constructor() {
    actual fun createSettings(): ObservableSettings {
        val ctx = AndroidContextHolder.context
            ?: throw IllegalStateException("Android context not set. Set AndroidContextHolder.context in your Application class.")
        return SharedPreferencesSettings.Factory(ctx).create()
    }
}

// A helper to be called from Android Application class if explicit context initialization is needed by any library.
// For multiplatform-settings-no-arg, this might not be strictly necessary for the default constructor.
// object AndroidAppContext { 
//     lateinit var application: Application
// } 

// companion object {
//     fun createSettings(context: Context): Settings {
//         return SettingsFactory(context).createSettings()
//     }
// } 
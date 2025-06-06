package com.webbanking.kmm.shared.settings

import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import android.app.Application // Required for the delegate
import android.content.Context

// To make SharedPreferencesSettings.Factory() work easily, we need an Application context.
// A common way is to have the Android Application class initialize a holder for the context,
// or pass it down. For multiplatform-settings-no-arg, it simplifies this by providing
// a factory that can often get the context itself if the library is initialized correctly
// in the Android app (e.g. by being present).
// Let's use the direct factory from the no-arg variant.

class SettingsFactory(private val context: Context) {
    fun createSettings(): Settings {
        // The SharedPreferencesSettings.Factory() from the no-arg artifact should work
        // if the Android Application context is available to it implicitly or through library setup.
        // If issues arise, one might need to ensure Application context is set for the library or pass it.
        return SharedPreferencesSettings.Factory().create("web_banking_settings")
    }
}

// A helper to be called from Android Application class if explicit context initialization is needed by any library.
// For multiplatform-settings-no-arg, this might not be strictly necessary for the default constructor.
// object AndroidAppContext { 
//     lateinit var application: Application
// } 

companion object {
    fun createSettings(context: Context): Settings {
        return SettingsFactory(context).createSettings()
    }
} 
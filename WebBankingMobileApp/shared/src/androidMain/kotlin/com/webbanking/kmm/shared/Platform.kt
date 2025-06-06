package com.webbanking.kmm.shared

import android.content.Context
import android.os.Build

actual class Platform {
    actual val osName: String = "Android"
    actual val osVersion: String = Build.VERSION.RELEASE
    actual val deviceModel: String = Build.MODEL
    actual val density: Int get() {
        // TODO: Provide Android context to access display metrics, or inject context properly
        throw NotImplementedError("Android context is required to get display density. Pass context explicitly or use dependency injection.")
    }
    actual val platform: String = "Android ${Build.VERSION.RELEASE}"
    actual val appVersion: String = "1.0.0-android" // TODO: Read from BuildConfig or package info if needed
} 
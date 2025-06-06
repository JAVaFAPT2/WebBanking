package com.webbanking.kmm.shared

import android.os.Build
// You might need to pass android.content.Context to get PackageManager for appVersion
// For simplicity, we'll use a placeholder here or you can pass context via constructor.

actual class Platform actual constructor() {
    actual val platform: String = "Android ${Build.VERSION.SDK_INT}"
    actual val appVersion: String = "1.0.0-android" // Replace with actual app version retrieval if needed
} 
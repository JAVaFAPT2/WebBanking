package com.webbanking.kmm.shared

actual class Platform actual constructor() {
    actual val osName: String = "JVM"
    actual val osVersion: String = System.getProperty("os.version") ?: "unknown"
    actual val deviceModel: String = System.getProperty("os.name") ?: "unknown"
    actual val density: Int = 1
    actual val platform: String = "JVM ${System.getProperty("os.version") ?: "unknown"}"
    actual val appVersion: String = "1.0.0-jvm"
} 
package com.webbanking.kmm.shared.network

actual object PlatformSpecificNetworkConfig {
    // 10.0.2.2 is the special alias for the host machine's localhost from the Android emulator.
    // Using the API Gateway running on port 8081
    actual val baseUrl: String = "http://10.0.2.2:8081"
} 
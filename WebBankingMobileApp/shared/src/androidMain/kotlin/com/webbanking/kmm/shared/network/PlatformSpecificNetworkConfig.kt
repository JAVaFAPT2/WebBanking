package com.webbanking.kmm.shared.network

actual object PlatformSpecificNetworkConfig {
    // 10.0.2.2 is the special alias for the host machine's localhost from the Android emulator.
    // Ensure your API Gateway is running on port 8080 of your host machine.
    actual val baseUrl: String = "http://127.0.0.1:8080/api"
} 
package com.webbanking.kmm.shared.network

actual object PlatformSpecificNetworkConfig {
    // For iOS simulator running on the same Mac as the backend server,
    // localhost or 127.0.0.1 should work.
    // Ensure your API Gateway is running on port 8080.
    actual val baseUrl: String = "http://localhost:8080/api"
    // For physical iOS devices, you'd need to use your computer's local network IP address.
    // This would require a more dynamic configuration mechanism.
} 
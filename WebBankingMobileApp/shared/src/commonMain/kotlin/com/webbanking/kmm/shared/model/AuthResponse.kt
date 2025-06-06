package com.webbanking.kmm.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String,
    val userId: String, // Or user ID, depending on what your backend returns
    val username: String,
    val roles: List<String> = emptyList() // Example: if your backend returns user roles
) 
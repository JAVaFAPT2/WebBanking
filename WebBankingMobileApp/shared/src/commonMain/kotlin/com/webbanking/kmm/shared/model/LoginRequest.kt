package com.webbanking.kmm.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val email: String, // Assuming email can also be used for login, adjust as per your backend
    val password: String
) 
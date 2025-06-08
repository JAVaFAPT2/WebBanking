package com.webbanking.kmm.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class RegistrationRequest(
    val username: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val firstName: String,
    val lastName: String,
    val address: String,
    val phoneNumber: String
) 
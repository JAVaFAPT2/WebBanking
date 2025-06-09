package com.webbanking.kmm.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val Street: String,
    val City: String,
    val State: String,
    val ZipCode: String,
    val Country: String
)

@Serializable
data class RegistrationRequest(
    val Username: String,
    val Email: String,
    val Password: String,
    val FirstName: String,
    val LastName: String,
    val PhoneNumber: String,
    val DateOfBirth: String, // Format: "1990-01-01T00:00:00Z"
    val Address: Address
) 
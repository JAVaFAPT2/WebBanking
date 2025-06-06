package com.webbanking.kmm.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class UserAccount(
    val accountId: String,
    val userId: String,
    val accountType: String, // e.g., SAVINGS, CHECKING
    val balance: Double,
    val currency: String, // e.g., USD, EUR
    val createdAt: String, // Or use kotlinx-datetime for proper date/time handling
    val status: String // e.g., ACTIVE, BLOCKED, CLOSED
) 
package com.webbanking.kmm.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val transactionId: String,
    val accountId: String,
    val type: String, // e.g., DEBIT, CREDIT, TRANSFER
    val amount: Double,
    val currency: String,
    val description: String? = null,
    val transactionDate: String, // Or use kotlinx-datetime
    val status: String // e.g. PENDING, COMPLETED, FAILED
) 
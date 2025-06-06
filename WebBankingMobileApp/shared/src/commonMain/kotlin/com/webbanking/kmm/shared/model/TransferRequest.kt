package com.webbanking.kmm.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class TransferRequest(
    val fromAccountId: String,
    val toAccountId: String,
    val amount: Double,
    val currency: String,
    val description: String? = null,
    val otp: String? = null // Optional: if OTP is required for transfers
) 
package com.webbanking.kmm.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class GenericApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val errorCode: String? = null // e.g., INSUFFICIENT_FUNDS, INVALID_OTP
) 
package com.webbanking.kmm.shared.repository

import com.webbanking.kmm.shared.model.GenericApiResponse
import com.webbanking.kmm.shared.model.Transaction
import com.webbanking.kmm.shared.model.TransferRequest

interface TransferRepository {
    // Assumes authentication token is handled
    suspend fun initiateTransfer(transferRequest: TransferRequest): NetworkResult<GenericApiResponse<Transaction>>
    // You might add other methods like getTransferStatus(transferId: String), etc.
} 
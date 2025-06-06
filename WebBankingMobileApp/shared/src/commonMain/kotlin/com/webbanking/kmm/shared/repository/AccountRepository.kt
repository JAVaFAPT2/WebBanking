package com.webbanking.kmm.shared.repository

import com.webbanking.kmm.shared.model.Transaction
import com.webbanking.kmm.shared.model.UserAccount

interface AccountRepository {
    // Assumes authentication token is handled, e.g., added via an interceptor or directly in requests
    suspend fun getUserAccounts(): NetworkResult<List<UserAccount>>
    suspend fun getAccountDetails(accountId: String): NetworkResult<UserAccount>
    suspend fun getAccountTransactions(accountId: String, page: Int = 1, size: Int = 20): NetworkResult<List<Transaction>>
} 
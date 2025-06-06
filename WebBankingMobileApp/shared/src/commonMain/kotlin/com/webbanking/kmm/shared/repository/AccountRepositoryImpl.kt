package com.webbanking.kmm.shared.repository

import com.webbanking.kmm.shared.model.Transaction
import com.webbanking.kmm.shared.model.UserAccount
import com.webbanking.kmm.shared.network.ApiClient
import com.webbanking.kmm.shared.settings.AuthTokenManager
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

// The global currentAuthToken placeholder is now removed.
// Token will be fetched from AuthTokenManager.

class AccountRepositoryImpl : AccountRepository {

    private val httpClient = ApiClient.client

    private fun HttpRequestBuilder.addAuthHeader() {
        AuthTokenManager.getAuthToken()?.let { token ->
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    override suspend fun getUserAccounts(): NetworkResult<List<UserAccount>> {
        if (!AuthTokenManager.hasToken()) {
            return NetworkResult.Error(Exception("Not authenticated. No token found."))
        }
        return try {
            val response = httpClient.get(ApiClient.constructUrl("accounts")) {
                addAuthHeader()
            }
            if (response.status == HttpStatusCode.OK) {
                NetworkResult.Success(response.body())
            } else {
                NetworkResult.Error(Exception("Failed to fetch accounts: ${response.status.description}"))
            }
        } catch (e: Exception) {
            NetworkResult.Error(e, "Network error fetching accounts: ${e.message}")
        }
    }

    override suspend fun getAccountDetails(accountId: String): NetworkResult<UserAccount> {
        if (!AuthTokenManager.hasToken()) {
            return NetworkResult.Error(Exception("Not authenticated. No token found."))
        }
        return try {
            val response = httpClient.get(ApiClient.constructUrl("accounts/$accountId")) {
                addAuthHeader()
            }
            if (response.status == HttpStatusCode.OK) {
                NetworkResult.Success(response.body())
            } else {
                NetworkResult.Error(Exception("Failed to fetch account details: ${response.status.description}"))
            }
        } catch (e: Exception) {
            NetworkResult.Error(e, "Network error fetching account details: ${e.message}")
        }
    }

    override suspend fun getAccountTransactions(accountId: String, page: Int, size: Int): NetworkResult<List<Transaction>> {
        if (!AuthTokenManager.hasToken()) {
            return NetworkResult.Error(Exception("Not authenticated. No token found."))
        }
        return try {
            val response = httpClient.get(ApiClient.constructUrl("accounts/$accountId/transactions")) {
                addAuthHeader()
                parameter("page", page)
                parameter("size", size)
            }
            if (response.status == HttpStatusCode.OK) {
                NetworkResult.Success(response.body())
            } else {
                NetworkResult.Error(Exception("Failed to fetch transactions: ${response.status.description}"))
            }
        } catch (e: Exception) {
            NetworkResult.Error(e, "Network error fetching transactions: ${e.message}")
        }
    }
} 
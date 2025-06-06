package com.webbanking.kmm.shared.repository

import com.webbanking.kmm.shared.model.GenericApiResponse
import com.webbanking.kmm.shared.model.Transaction
import com.webbanking.kmm.shared.model.TransferRequest
import com.webbanking.kmm.shared.network.ApiClient
import com.webbanking.kmm.shared.settings.AuthTokenManager
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class TransferRepositoryImpl : TransferRepository {

    private val httpClient = ApiClient.client

    private fun HttpRequestBuilder.addAuthHeader() {
        AuthTokenManager.getAuthToken()?.let { token -> 
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    override suspend fun initiateTransfer(transferRequest: TransferRequest): NetworkResult<GenericApiResponse<Transaction>> {
        if (!AuthTokenManager.hasToken()) {
            return NetworkResult.Error(Exception("Not authenticated. No token found."))
        }
        return try {
            val response = httpClient.post(ApiClient.constructUrl("transfers")) {
                contentType(ContentType.Application.Json)
                setBody(transferRequest)
                addAuthHeader()
            }
            if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) {
                NetworkResult.Success(response.body())
            } else {
                // Attempt to parse a GenericApiResponse for error details if possible
                try {
                    val errorResponse: GenericApiResponse<Transaction> = response.body()
                    NetworkResult.Error(Exception(errorResponse.message ?: "Transfer failed: ${response.status.description}"))
                } catch (parseException: Exception) {
                    NetworkResult.Error(Exception("Transfer failed: ${response.status.description} (Could not parse error body)"))
                }
            }
        } catch (e: Exception) {
            NetworkResult.Error(e, "Network error during transfer: ${e.message}")
        }
    }
} 
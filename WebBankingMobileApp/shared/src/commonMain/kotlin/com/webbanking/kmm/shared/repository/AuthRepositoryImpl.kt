package com.webbanking.kmm.shared.repository

import com.webbanking.kmm.shared.model.AuthResponse
import com.webbanking.kmm.shared.model.GenericApiResponse
import com.webbanking.kmm.shared.model.LoginRequest
import com.webbanking.kmm.shared.model.RegistrationRequest
import com.webbanking.kmm.shared.network.ApiClient
import com.webbanking.kmm.shared.settings.AuthTokenManager
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class AuthRepositoryImpl : AuthRepository {

    private val httpClient = ApiClient.client

    override suspend fun login(loginRequest: LoginRequest): NetworkResult<AuthResponse> {
        return try {
            val response = httpClient.post(ApiClient.constructUrl("api/auth/login")) {
                contentType(ContentType.Application.Json)
                setBody(loginRequest)
            }
            if (response.status == HttpStatusCode.OK) {
                val authData = response.body<AuthResponse>()
                AuthTokenManager.saveAuthToken(authData.token)
                NetworkResult.Success(authData)
            } else {
                NetworkResult.Error(Exception("Login failed: ${response.status.description}"))
            }
        } catch (e: Exception) {
            NetworkResult.Error(e, "Network error during login: ${e.message}")
        }
    }

    override suspend fun register(registrationRequest: RegistrationRequest): NetworkResult<GenericApiResponse<AuthResponse>> {
        return try {
            val response = httpClient.post(ApiClient.constructUrl("api/auth/register")) {
                contentType(ContentType.Application.Json)
                setBody(registrationRequest)
            }
            if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) {
                val apiResponse = response.body<GenericApiResponse<AuthResponse>>()
                NetworkResult.Success(apiResponse)
            } else {
                NetworkResult.Error(Exception("Registration failed: ${response.status.description}"))
            }
        } catch (e: Exception) {
            NetworkResult.Error(e, "Network error during registration: ${e.message}")
        }
    }

    override suspend fun logout(): NetworkResult<GenericApiResponse<Unit>> {
        return try {
            AuthTokenManager.clearAuthToken()
            println("User logged out and token cleared.")
            NetworkResult.Success(GenericApiResponse(success = true, message = "Logged out successfully"))
        } catch (e: Exception) {
            AuthTokenManager.clearAuthToken()
            NetworkResult.Error(e, "Logout error: ${e.message}. Token cleared locally.")
        }
    }
} 
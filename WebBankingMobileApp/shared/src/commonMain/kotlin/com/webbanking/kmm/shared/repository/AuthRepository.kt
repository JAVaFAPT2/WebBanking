package com.webbanking.kmm.shared.repository

import com.webbanking.kmm.shared.model.AuthResponse
import com.webbanking.kmm.shared.model.GenericApiResponse
import com.webbanking.kmm.shared.model.LoginRequest
// Import other models like RegistrationRequest if you add that functionality

interface AuthRepository {
    suspend fun login(loginRequest: LoginRequest): NetworkResult<AuthResponse>
    // suspend fun register(registrationRequest: RegistrationRequest): NetworkResult<GenericApiResponse<AuthResponse>> // Example for registration
    suspend fun logout(): NetworkResult<GenericApiResponse<Unit>> // Example for logout
} 
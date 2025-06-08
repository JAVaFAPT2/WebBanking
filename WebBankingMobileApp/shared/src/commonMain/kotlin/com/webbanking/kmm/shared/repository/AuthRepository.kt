package com.webbanking.kmm.shared.repository

import com.webbanking.kmm.shared.model.AuthResponse
import com.webbanking.kmm.shared.model.GenericApiResponse
import com.webbanking.kmm.shared.model.LoginRequest
import com.webbanking.kmm.shared.model.RegistrationRequest
// Import other models like RegistrationRequest if you add that functionality

interface AuthRepository {
    suspend fun login(loginRequest: LoginRequest): NetworkResult<AuthResponse>
    suspend fun register(registrationRequest: RegistrationRequest): NetworkResult<GenericApiResponse<AuthResponse>>
    suspend fun logout(): NetworkResult<GenericApiResponse<Unit>> // Example for logout
} 
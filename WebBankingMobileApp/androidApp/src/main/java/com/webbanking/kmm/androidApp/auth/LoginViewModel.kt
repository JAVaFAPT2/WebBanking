package com.webbanking.kmm.androidApp.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.webbanking.kmm.shared.model.AuthResponse
import com.webbanking.kmm.shared.model.LoginRequest
import com.webbanking.kmm.shared.repository.AuthRepository
import com.webbanking.kmm.shared.repository.AuthRepositoryImpl // Direct instantiation for simplicity
import com.webbanking.kmm.shared.repository.NetworkResult
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val authResponse: AuthResponse) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(private val authRepository: AuthRepository = AuthRepositoryImpl()) : ViewModel() {

    var uiState by mutableStateOf<LoginUiState>(LoginUiState.Idle)
        private set

    var email by mutableStateOf("user@example.com") // Sample default
    var password by mutableStateOf("password123") // Sample default

    fun login() {
        if (email.isBlank() || password.isBlank()) {
            uiState = LoginUiState.Error("Email and password cannot be empty.")
            return
        }

        viewModelScope.launch {
            uiState = LoginUiState.Loading
            val result = authRepository.login(LoginRequest(username = email, email = email, password = password))
            uiState = when (result) {
                is NetworkResult.Success -> {
                    // Token is now saved by AuthRepositoryImpl via AuthTokenManager
                    LoginUiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    LoginUiState.Error(result.message ?: "An unknown error occurred.")
                }
                is NetworkResult.Loading -> LoginUiState.Loading // Should not happen here
            }
        }
    }
    
    fun performLogout(){
        viewModelScope.launch {
            // uiState = LoginUiState.Loading // Optional: show loading during logout
            authRepository.logout() // This now clears the token via AuthTokenManager
            uiState = LoginUiState.Idle // Reset to Idle or navigate to login screen
            // Potentially emit a different state like LogoutSuccess if needed for UI reaction
        }
    }
} 
package com.webbanking.kmm.androidApp.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.webbanking.kmm.shared.model.AuthResponse
import com.webbanking.kmm.shared.model.LoginRequest
import com.webbanking.kmm.shared.model.RegistrationRequest
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

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    data class Success(val message: String?) : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}

class LoginViewModel(private val authRepository: AuthRepository = AuthRepositoryImpl()) : ViewModel() {

    var uiState by mutableStateOf<LoginUiState>(LoginUiState.Idle)
        private set

    var registerUiState by mutableStateOf<RegisterUiState>(RegisterUiState.Idle)
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

    fun register(username: String, email: String, password: String, confirmPassword: String, firstName: String, lastName: String, address: String, phoneNumber: String) {
        if (username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() || firstName.isBlank() || lastName.isBlank() || address.isBlank() || phoneNumber.isBlank()) {
            registerUiState = RegisterUiState.Error("All fields are required.")
            return
        }
        if (password != confirmPassword) {
            registerUiState = RegisterUiState.Error("Passwords do not match.")
            return
        }
        viewModelScope.launch {
            registerUiState = RegisterUiState.Loading
            val result = authRepository.register(
                RegistrationRequest(
                    username = username,
                    email = email,
                    password = password,
                    confirmPassword = confirmPassword,
                    firstName = firstName,
                    lastName = lastName,
                    address = address,
                    phoneNumber = phoneNumber
                )
            )
            registerUiState = when (result) {
                is NetworkResult.Success -> {
                    if (result.data.success) {
                        RegisterUiState.Success(result.data.message)
                    } else {
                        RegisterUiState.Error(result.data.message ?: "Registration failed.")
                    }
                }
                is NetworkResult.Error -> {
                    RegisterUiState.Error(result.message ?: "An unknown error occurred.")
                }
                is NetworkResult.Loading -> RegisterUiState.Loading // Should not happen here
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

    fun resetRegisterUiState() {
        registerUiState = RegisterUiState.Idle
    }
} 
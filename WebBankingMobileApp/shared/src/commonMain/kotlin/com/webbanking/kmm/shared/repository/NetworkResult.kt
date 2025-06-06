package com.webbanking.kmm.shared.repository

sealed class NetworkResult<out T : Any> {
    data class Success<out T : Any>(val data: T) : NetworkResult<T>()
    data class Error(val exception: Exception, val message: String? = null) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>() // Optional: for UI to show loading state
} 
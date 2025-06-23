package com.webbanking.kmm.androidApp.transfer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.webbanking.kmm.shared.model.TransferRequest
import com.webbanking.kmm.shared.repository.NetworkResult
import com.webbanking.kmm.shared.repository.TransferRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class TransferUiState {
    object Idle : TransferUiState()
    object Loading : TransferUiState()
    object Success : TransferUiState()
    data class Error(val message: String) : TransferUiState()
}

class TransferViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val transferRepository: TransferRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TransferUiState>(TransferUiState.Idle)
    val uiState: StateFlow<TransferUiState> = _uiState

    private val fromAccountId: String = savedStateHandle["fromAccountId"] ?: ""

    fun transfer(toAccountId: String, amount: Double, currency: String = "USD", description: String? = null) {
        if (toAccountId.isBlank() || amount <= 0) {
            _uiState.value = TransferUiState.Error("Invalid input")
            return
        }
        viewModelScope.launch {
            _uiState.value = TransferUiState.Loading
            val result = transferRepository.initiateTransfer(
                TransferRequest(
                    fromAccountId = fromAccountId,
                    toAccountId = toAccountId,
                    amount = amount,
                    currency = currency,
                    description = description
                )
            )
            _uiState.value = when (result) {
                is NetworkResult.Success -> TransferUiState.Success
                is NetworkResult.Error -> TransferUiState.Error(result.message ?: "Transfer failed")
                else -> TransferUiState.Idle
            }
        }
    }
} 
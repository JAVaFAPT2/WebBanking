package com.webbanking.kmm.androidApp.account

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.webbanking.kmm.shared.model.UserAccount
import com.webbanking.kmm.shared.model.Transaction
import com.webbanking.kmm.shared.repository.AccountRepository
import com.webbanking.kmm.shared.repository.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

sealed class AccountDetailsUiState {
    object Loading : AccountDetailsUiState()
    data class Success(val account: UserAccount, val transactions: List<Transaction>) : AccountDetailsUiState()
    data class Error(val message: String) : AccountDetailsUiState()
}

class AccountDetailsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AccountDetailsUiState>(AccountDetailsUiState.Loading)
    val uiState: StateFlow<AccountDetailsUiState> = _uiState

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private var currentPage = 1
    private var hasMore = true

    private val accountId: String = savedStateHandle["accountId"] ?: ""

    init {
        fetchAccountDetails()
    }

    private fun fetchAccountDetails() {
        viewModelScope.launch {
            when (val result = accountRepository.getAccountDetails(accountId)) {
                is NetworkResult.Success -> {
                    _uiState.value = AccountDetailsUiState.Success(result.data, emptyList())
                    refreshTransactions()
                }
                is NetworkResult.Error -> _uiState.value = AccountDetailsUiState.Error(result.message ?: "Error loading details")
                else -> Unit
            }
        }
    }

    fun refreshTransactions() {
        viewModelScope.launch {
            _isRefreshing.value = true
            currentPage = 1
            hasMore = true
            when (val txResult = accountRepository.getAccountTransactions(accountId, page = currentPage)) {
                is NetworkResult.Success -> _transactions.value = txResult.data
                else -> _transactions.value = emptyList()
            }
            _isRefreshing.value = false
        }
    }

    fun loadNextPage() {
        if (!hasMore || _uiState.value is AccountDetailsUiState.Loading) return
        viewModelScope.launch {
            currentPage++
            when (val txResult = accountRepository.getAccountTransactions(accountId, page = currentPage)) {
                is NetworkResult.Success -> {
                    if (txResult.data.isEmpty()) {
                        hasMore = false
                    } else {
                        _transactions.update { it + txResult.data }
                    }
                }
                else -> hasMore = false
            }
        }
    }
} 
package com.webbanking.kmm.androidApp.account

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.webbanking.kmm.shared.model.UserAccount
import com.webbanking.kmm.shared.repository.AccountRepository
import com.webbanking.kmm.shared.repository.AccountRepositoryImpl // Direct instantiation
import com.webbanking.kmm.shared.repository.NetworkResult
import kotlinx.coroutines.launch

sealed class AccountListUiState {
    object Loading : AccountListUiState()
    data class Success(val accounts: List<UserAccount>) : AccountListUiState()
    data class Error(val message: String) : AccountListUiState()
    object NoAccounts : AccountListUiState() // For when the list is empty but no error
}

class AccountViewModel(private val accountRepository: AccountRepository = AccountRepositoryImpl()) : ViewModel() {

    var uiState by mutableStateOf<AccountListUiState>(AccountListUiState.Loading)
        private set

    fun fetchUserAccounts() {
        viewModelScope.launch {
            uiState = AccountListUiState.Loading
            when (val result = accountRepository.getUserAccounts()) {
                is NetworkResult.Success -> {
                    if (result.data.isEmpty()) {
                        uiState = AccountListUiState.NoAccounts
                    } else {
                        uiState = AccountListUiState.Success(result.data)
                    }
                }
                is NetworkResult.Error -> {
                    uiState = AccountListUiState.Error(result.message ?: "Failed to load accounts.")
                }
                is NetworkResult.Loading -> {
                    // Handled by initial state or explicit setting above
                }
            }
        }
    }
} 
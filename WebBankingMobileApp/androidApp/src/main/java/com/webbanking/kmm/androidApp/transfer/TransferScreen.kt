package com.webbanking.kmm.androidApp.transfer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.webbanking.kmm.androidApp.ui.components.BankTextField
import com.webbanking.kmm.androidApp.ui.components.PrimaryButton
import androidx.compose.material3.CircularProgressIndicator
import org.koin.androidx.compose.koinViewModel
import com.webbanking.kmm.androidApp.ui.components.SecondaryButton

@Composable
fun TransferScreen(
    fromAccountId: String,
    onTransferComplete: () -> Unit,
    onCancel: () -> Unit
) {
    val viewModel: TransferViewModel = koinViewModel()
    var toAccountId by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Transfer Funds", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        Text("From Account: $fromAccountId", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(16.dp))

        BankTextField(
            value = toAccountId,
            onValueChange = { toAccountId = it },
            label = "To Account ID",
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        BankTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = "Amount",
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))

        when (uiState) {
            TransferUiState.Success -> {
                Text("Transfer Successful!", color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))
                PrimaryButton(text = "Done", onClick = onTransferComplete)
            }
            TransferUiState.Loading -> {
                CircularProgressIndicator()
            }
            is TransferUiState.Error -> {
                Text((uiState as TransferUiState.Error).message, color = MaterialTheme.colorScheme.error)
            }
            else -> {
                PrimaryButton(
                    text = "Transfer",
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        viewModel.transfer(toAccountId, amount)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    loading = uiState == TransferUiState.Loading
                )
                Spacer(Modifier.height(16.dp))
                SecondaryButton(text = "Cancel", onClick = onCancel, modifier = Modifier.fillMaxWidth())
            }
        }
    }
} 
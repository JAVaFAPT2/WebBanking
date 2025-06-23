package com.webbanking.kmm.androidApp.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import com.webbanking.kmm.androidApp.ui.components.PrimaryButton
import com.webbanking.kmm.androidApp.ui.components.SecondaryButton
import com.webbanking.kmm.androidApp.account.AccountDetailsUiState
import com.webbanking.kmm.androidApp.account.AccountDetailsViewModel
import com.webbanking.kmm.shared.model.Transaction
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.collectAsState
import kotlinx.coroutines.flow.snapshotFlow

@Composable
fun AccountDetailsScreen(
    accountId: String,
    onTransferClick: (String) -> Unit,
    onBack: () -> Unit = {}
) {
    val viewModel: AccountDetailsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is AccountDetailsUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is AccountDetailsUiState.Error -> {
            val message = (uiState as AccountDetailsUiState.Error).message
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("Error: $message", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                PrimaryButton(text = "Back", onClick = onBack)
            }
        }
        is AccountDetailsUiState.Success -> {
            val account = (uiState as AccountDetailsUiState.Success).account
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                Text("Account Details", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(16.dp))
                Text("Type: ${account.accountType}", style = MaterialTheme.typography.bodyLarge)
                Text("Balance: ${account.balance} ${account.currency}", style = MaterialTheme.typography.bodyLarge)
                Text("Status: ${account.status}", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(24.dp))
                Text("Transactions", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                val listState = rememberLazyListState()
                val transactions by viewModel.transactions.collectAsState()

                val swipeState = rememberSwipeRefreshState(isRefreshing = viewModel.isRefreshing.collectAsState().value)

                SwipeRefresh(state = swipeState, onRefresh = { viewModel.refreshTransactions() }) {
                    if (transactions.isEmpty()) {
                        Text("No transactions")
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxHeight(0.5f), state = listState) {
                            items(transactions) { tx ->
                                TransactionRow(tx)
                            }
                        }
                    }
                }

                // Load next page when scrolled to end
                LaunchedEffect(listState) {
                    snapshotFlow { listState.layoutInfo }
                        .collect { info ->
                            val total = info.totalItemsCount
                            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
                            if (lastVisible >= total - 3) {
                                viewModel.loadNextPage()
                            }
                        }
                }
                Spacer(Modifier.height(24.dp))
                PrimaryButton(text = "Transfer Funds", onClick = { onTransferClick(account.accountId) })
            }
        }
    }
}

@Composable
fun TransactionRow(tx: Transaction) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val isCredit = tx.type.uppercase() == "CREDIT"
            Icon(
                imageVector = if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                contentDescription = null,
                tint = if (isCredit) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(tx.description ?: "Transaction", style = MaterialTheme.typography.bodyMedium)
                Text(tx.transactionDate, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp))
            }
        }
        Text(
            text = "${tx.amount} ${tx.currency}",
            color = if (tx.type.uppercase() == "CREDIT") Color(0xFF2E7D32) else Color(0xFFC62828),
            style = MaterialTheme.typography.bodyMedium
        )
    }
} 
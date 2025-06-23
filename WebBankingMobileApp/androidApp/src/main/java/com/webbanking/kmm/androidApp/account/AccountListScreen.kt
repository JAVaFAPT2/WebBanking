package com.webbanking.kmm.androidApp.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import com.webbanking.kmm.shared.model.UserAccount
import com.webbanking.kmm.androidApp.ui.components.SecondaryButton
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.snapshotFlow
import androidx.compose.foundation.lazy.LazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountListScreen(
    accountViewModel: AccountViewModel = koinViewModel(),
    onLogout: () -> Unit,
    onAccountSelected: (UserAccount) -> Unit = {}
) {
    val uiState by rememberUpdatedState(accountViewModel.uiState)

    LaunchedEffect(Unit) { // Call fetchUserAccounts when the screen is first composed
        accountViewModel.fetchUserAccounts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Accounts") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is AccountListUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is AccountListUiState.Success -> {
                    if (state.accounts.isEmpty()) {
                        Text("No accounts found.")
                    } else {
                        val listState = rememberLazyListState()
                        val swipeState = rememberSwipeRefreshState(isRefreshing = accountViewModel.isRefreshing.collectAsState().value)

                        SwipeRefresh(state = swipeState, onRefresh = { accountViewModel.refresh() }) {
                            AccountListView(accounts = state.accounts, onAccountSelected, listState)
                        }

                        // detect end
                        LaunchedEffect(listState) {
                            snapshotFlow { listState.layoutInfo }
                                .collect { info ->
                                    val total = info.totalItemsCount
                                    val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
                                    if (last >= total - 3) accountViewModel.loadNextPage()
                                }
                        }
                    }
                }
                is AccountListUiState.NoAccounts -> {
                    Text("You don't have any accounts yet.")
                }
                is AccountListUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        SecondaryButton(text = "Retry", onClick = { accountViewModel.fetchUserAccounts() })
                    }
                }
            }
        }
    }
}

@Composable
fun AccountListView(accounts: List<UserAccount>, onAccountSelected: (UserAccount) -> Unit, listState: LazyListState) {
    LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
        items(accounts) { account ->
            AccountItem(account = account, onClick = { onAccountSelected(account) })
            Divider()
        }
    }
}

@Composable
fun AccountItem(account: UserAccount, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(account.accountType, style = MaterialTheme.typography.titleMedium)
                Text("ID: ${account.accountId}", style = MaterialTheme.typography.bodySmall)
                Text("Status: ${account.status}", style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = "${account.balance} ${account.currency}",
                style = MaterialTheme.typography.titleMedium,
                color = if (account.balance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
} 
package com.webbanking.kmm.androidApp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.webbanking.kmm.androidApp.account.AccountListScreen
import com.webbanking.kmm.androidApp.auth.LoginUiState
import com.webbanking.kmm.androidApp.auth.LoginViewModel
import com.webbanking.kmm.shared.Platform // Example of using another shared class
import com.webbanking.kmm.shared.settings.AuthTokenManager // For checking initial auth state

// Simple navigation state
enum class Screen {
    Login,
    AccountList
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    var currentScreen by remember { 
                        mutableStateOf(if (AuthTokenManager.hasToken()) Screen.AccountList else Screen.Login) 
                    }
                    val loginViewModel: LoginViewModel = viewModel()

                    when (currentScreen) {
                        Screen.Login -> LoginScreen(
                            loginViewModel = loginViewModel,
                            onLoginSuccess = { currentScreen = Screen.AccountList }
                        )
                        Screen.AccountList -> AccountListScreen(
                            // accountViewModel is implicitly available via viewModel() in AccountListScreen
                            onLogout = {
                                loginViewModel.performLogout() // Clears token
                                currentScreen = Screen.Login
                            }
                        )
                    }
                }
            }
        }
        println("App started on: ${Platform().platform} with version ${Platform().appVersion}")
    }
}

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel, // Pass the ViewModel instance
    onLoginSuccess: () -> Unit
) {
    val uiState = loginViewModel.uiState
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        when (uiState) {
            is LoginUiState.Success -> {
                Toast.makeText(context, "Login Successful! User: ${uiState.authResponse.username}", Toast.LENGTH_LONG).show()
                onLoginSuccess() // Navigate on success
            }
            is LoginUiState.Error -> {
                Toast.makeText(context, "Login Failed: ${uiState.message}", Toast.LENGTH_LONG).show()
            }
            else -> Unit 
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Web Banking Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = loginViewModel.email,
            onValueChange = { loginViewModel.email = it },
            label = { Text("Email/Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = loginViewModel.password,
            onValueChange = { loginViewModel.password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { loginViewModel.login() },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState != LoginUiState.Loading
        ) {
            if (uiState == LoginUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Login")
            }
        }
    }
}

@Composable
fun MyApplicationTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
        typography = Typography(), 
        content = content
    )
}

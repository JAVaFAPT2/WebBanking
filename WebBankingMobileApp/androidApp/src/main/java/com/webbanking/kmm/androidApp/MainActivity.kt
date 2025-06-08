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
import com.webbanking.kmm.androidApp.auth.RegisterUiState
import com.webbanking.kmm.shared.Platform // Example of using another shared class
import com.webbanking.kmm.shared.settings.AuthTokenManager // For checking initial auth state
import com.webbanking.kmm.shared.settings.AndroidContextHolder
import com.webbanking.kmm.shared.settings.SettingsFactory

// Simple navigation state
enum class Screen {
    Login,
    Register,
    AccountList
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize AuthTokenManager for KMM settings
        AndroidContextHolder.context = applicationContext
        AuthTokenManager.init(SettingsFactory())

        // Determine initial screen after initialization
        val initialScreen = if (AuthTokenManager.hasToken()) Screen.AccountList else Screen.Login

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    var currentScreen by remember { mutableStateOf(initialScreen) }
                    val loginViewModel: LoginViewModel = viewModel()

                    when (currentScreen) {
                        Screen.Login -> LoginScreen(
                            loginViewModel = loginViewModel,
                            onLoginSuccess = { currentScreen = Screen.AccountList },
                            onRegisterClick = { currentScreen = Screen.Register }
                        )
                        Screen.Register -> RegisterScreen(
                            onRegisterSuccess = { currentScreen = Screen.Login },
                            onBackToLogin = { currentScreen = Screen.Login }
                        )
                        Screen.AccountList -> AccountListScreen(
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
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
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
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onRegisterClick, modifier = Modifier.align(Alignment.End)) {
            Text("Don't have an account? Register")
        }
    }
}

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val loginViewModel: LoginViewModel = viewModel()
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    val registerUiState = loginViewModel.registerUiState
    val context = LocalContext.current

    LaunchedEffect(registerUiState) {
        when (registerUiState) {
            is RegisterUiState.Success -> {
                Toast.makeText(context, registerUiState.message ?: "Registration successful!", Toast.LENGTH_LONG).show()
                onRegisterSuccess()
                loginViewModel.resetRegisterUiState()
            }
            is RegisterUiState.Error -> {
                // Show error via UI below
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
        Text("Register", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (registerUiState is RegisterUiState.Error) {
            Text(registerUiState.message, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }
        Button(
            onClick = {
                loginViewModel.register(username, email, password, confirmPassword, firstName, lastName, address, phoneNumber)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = registerUiState != RegisterUiState.Loading
        ) {
            if (registerUiState == RegisterUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Register")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onBackToLogin, modifier = Modifier.align(Alignment.End)) {
            Text("Back to Login")
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

package com.webbanking.kmm.androidApp.auth.ui.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import org.koin.androidx.compose.koinViewModel
import com.webbanking.kmm.androidApp.ui.theme.DimensionTokens
import com.webbanking.kmm.androidApp.auth.LoginUiState
import com.webbanking.kmm.androidApp.auth.LoginViewModel
import com.webbanking.kmm.androidApp.ui.components.PrimaryButton
import com.webbanking.kmm.androidApp.ui.components.BankTextField

/**
 * Modernised Login screen that honours the design-system spacing tokens.
 */
@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
) {
    val uiState = loginViewModel.uiState
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        when (uiState) {
            is LoginUiState.Success -> {
                Toast.makeText(context, "Login Successful! User: ${'$'}{uiState.authResponse.username}", Toast.LENGTH_LONG).show()
                onLoginSuccess()
            }
            is LoginUiState.Error -> {
                Toast.makeText(context, "Login Failed: ${'$'}{uiState.message}", Toast.LENGTH_LONG).show()
            }
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DimensionTokens.Space16),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Web Banking Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(DimensionTokens.Space32))

        BankTextField(
            value = loginViewModel.email,
            onValueChange = { loginViewModel.email = it },
            label = "Email/Username",
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(DimensionTokens.Space16))

        BankTextField(
            value = loginViewModel.password,
            onValueChange = { loginViewModel.password = it },
            label = "Password",
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
        )
        Spacer(modifier = Modifier.height(DimensionTokens.Space24))

        PrimaryButton(
            text = "Login",
            onClick = { loginViewModel.login() },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState != LoginUiState.Loading,
            loading = uiState == LoginUiState.Loading
        )
        Spacer(modifier = Modifier.height(DimensionTokens.Space16))
        TextButton(onClick = onRegisterClick, modifier = Modifier.align(Alignment.End)) {
            Text("Don't have an account? Register")
        }
    }
} 
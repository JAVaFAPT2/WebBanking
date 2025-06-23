package com.webbanking.kmm.androidApp.auth.ui.register

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import com.webbanking.kmm.androidApp.auth.LoginViewModel
import com.webbanking.kmm.androidApp.auth.RegisterUiState
import com.webbanking.kmm.androidApp.ui.theme.DimensionTokens
import com.webbanking.kmm.androidApp.ui.components.PrimaryButton
import com.webbanking.kmm.androidApp.ui.components.BankTextField

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    loginViewModel: LoginViewModel = koinViewModel()
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var zipCode by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }

    val registerUiState = loginViewModel.registerUiState
    val context = LocalContext.current

    LaunchedEffect(registerUiState) {
        when (registerUiState) {
            is RegisterUiState.Success -> {
                Toast.makeText(context, registerUiState.message ?: "Registration successful!", Toast.LENGTH_LONG).show()
                onRegisterSuccess()
                loginViewModel.resetRegisterUiState()
            }
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DimensionTokens.Space16)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Register", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(DimensionTokens.Space32))

        fun Modifier.formFieldSpacer() = this.then(Modifier.height(DimensionTokens.Space16))

        BankTextField(username, { username = it }, label = "Username", modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.formFieldSpacer())
        BankTextField(email, { email = it }, label = "Email", modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.formFieldSpacer())
        BankTextField(password, { password = it }, label = "Password", modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
        Spacer(Modifier.formFieldSpacer())
        BankTextField(confirmPassword, { confirmPassword = it }, label = "Confirm Password", modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
        Spacer(Modifier.formFieldSpacer())
        BankTextField(firstName, { firstName = it }, label = "First Name", modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.formFieldSpacer())
        BankTextField(lastName, { lastName = it }, label = "Last Name", modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.formFieldSpacer())
        BankTextField(phoneNumber, { phoneNumber = it }, label = "Phone Number", modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.formFieldSpacer())
        BankTextField(dateOfBirth, { dateOfBirth = it }, label = "Date of Birth (YYYY-MM-DD)", modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.formFieldSpacer())
        BankTextField(street, { street = it }, label = "Street", modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.formFieldSpacer())
        BankTextField(city, { city = it }, label = "City", modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.formFieldSpacer())
        BankTextField(state, { state = it }, label = "State", modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.formFieldSpacer())
        BankTextField(zipCode, { zipCode = it }, label = "Zip Code", modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.formFieldSpacer())
        BankTextField(country, { country = it }, label = "Country", modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(DimensionTokens.Space24))

        if (registerUiState is RegisterUiState.Error) {
            Text(registerUiState.message, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(DimensionTokens.Space8))
        }

        val enabled = registerUiState != RegisterUiState.Loading

        PrimaryButton(
            text = "Register",
            onClick = {
                loginViewModel.register(
                    username,
                    email,
                    password,
                    confirmPassword,
                    firstName,
                    lastName,
                    phoneNumber,
                    dateOfBirth,
                    street,
                    city,
                    state,
                    zipCode,
                    country
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            loading = registerUiState == RegisterUiState.Loading
        )
        Spacer(modifier = Modifier.height(DimensionTokens.Space16))
        TextButton(onClick = onBackToLogin, modifier = Modifier.align(Alignment.End)) {
            Text("Back to Login")
        }
    }
} 
package com.webbanking.kmm.androidApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import com.webbanking.kmm.androidApp.navigation.BankNavGraph
import com.webbanking.kmm.androidApp.navigation.rememberBankNavController
import com.webbanking.kmm.shared.Platform // Example of using another shared class
import com.webbanking.kmm.shared.settings.AuthTokenManager // For checking initial auth state
import com.webbanking.kmm.shared.settings.AndroidContextHolder
import com.webbanking.kmm.shared.settings.SettingsFactory

// Navigation handled via NavController; enum removed.

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize AuthTokenManager for KMM settings
        AndroidContextHolder.context = applicationContext
        AuthTokenManager.init(SettingsFactory())

        setContent {
            com.webbanking.kmm.androidApp.ui.theme.BankTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberBankNavController()
                    BankNavGraph(navController = navController)
                }
            }
        }
        println("App started on: ${Platform().platform} with version ${Platform().appVersion}")
    }
}

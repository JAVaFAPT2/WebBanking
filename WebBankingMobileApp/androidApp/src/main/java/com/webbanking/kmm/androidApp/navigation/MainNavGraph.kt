package com.webbanking.kmm.androidApp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.webbanking.kmm.androidApp.account.AccountListScreen
import com.webbanking.kmm.androidApp.auth.ui.login.LoginScreen
import com.webbanking.kmm.androidApp.auth.ui.register.RegisterScreen
import com.webbanking.kmm.shared.settings.AuthTokenManager
import com.webbanking.kmm.androidApp.account.AccountDetailsScreen
import com.webbanking.kmm.androidApp.transfer.TransferScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val ACCOUNTS = "accounts"
    const val DETAILS = "details/{accountId}"
    const val TRANSFER = "transfer/{fromAccountId}"
}

@Composable
fun rememberBankNavController(): NavHostController = rememberNavController()

@Composable
fun BankNavGraph(navController: NavHostController) {
    val startDestination = if (AuthTokenManager.hasToken()) Routes.ACCOUNTS else Routes.LOGIN

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.ACCOUNTS) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.LOGIN) { popUpTo(Routes.REGISTER) { inclusive = true } }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }
        composable(Routes.ACCOUNTS) {
            AccountListScreen(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onAccountSelected = { account ->
                    navController.navigate("details/${account.accountId}")
                }
            )
        }
        composable(
            route = Routes.DETAILS,
            arguments = listOf(navArgument("accountId") { type = NavType.StringType })
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: return@composable
            AccountDetailsScreen(
                accountId = accountId,
                onTransferClick = { fromId ->
                    navController.navigate("transfer/${fromId}")
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.TRANSFER,
            arguments = listOf(navArgument("fromAccountId") { type = NavType.StringType })
        ) { backStackEntry ->
            val fromId = backStackEntry.arguments?.getString("fromAccountId") ?: return@composable
            TransferScreen(
                fromAccountId = fromId,
                onTransferComplete = { navController.popBackStack(Routes.ACCOUNTS, false) },
                onCancel = { navController.popBackStack() }
            )
        }
    }
} 
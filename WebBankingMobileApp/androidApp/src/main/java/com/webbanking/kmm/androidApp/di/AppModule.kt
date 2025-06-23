package com.webbanking.kmm.androidApp.di

import com.webbanking.kmm.androidApp.account.AccountDetailsViewModel
import com.webbanking.kmm.androidApp.account.AccountViewModel
import com.webbanking.kmm.androidApp.transfer.TransferViewModel
import com.webbanking.kmm.androidApp.auth.LoginViewModel
import com.webbanking.kmm.shared.repository.*
import com.webbanking.kmm.shared.di.sharedModule
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Repositories
    single<AuthRepository> { AuthRepositoryImpl() }
    single<AccountRepository> { AccountRepositoryImpl() }
    single<TransferRepository> { TransferRepositoryImpl() }

    // ViewModels
    viewModel { LoginViewModel(get()) }
    viewModel { AccountViewModel(get()) }
    viewModel { (handle: androidx.lifecycle.SavedStateHandle) -> AccountDetailsViewModel(handle, get()) }
    viewModel { (handle: androidx.lifecycle.SavedStateHandle) -> TransferViewModel(handle, get()) }
}

val androidModule = module { /* existing definitions? we have one module; we can combine*/ } 
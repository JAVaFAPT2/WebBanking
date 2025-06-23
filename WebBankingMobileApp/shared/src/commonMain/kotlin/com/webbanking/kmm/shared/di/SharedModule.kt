package com.webbanking.kmm.shared.di

import com.webbanking.kmm.shared.repository.*
import org.koin.dsl.module

val sharedModule = module {
    single<AuthRepository> { AuthRepositoryImpl() }
    single<AccountRepository> { AccountRepositoryImpl() }
    single<TransferRepository> { TransferRepositoryImpl() }
} 
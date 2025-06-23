package com.webbanking.kmm.shared.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module

fun initKoinIOS(additionalModules: List<Module> = emptyList()) {
    startKoin {
        modules(sharedModule + additionalModules)
    }
} 
package com.webbanking.kmm.androidApp

import android.app.Application
import com.webbanking.kmm.androidApp.di.appModule
import com.webbanking.kmm.androidApp.di.androidModule
import com.webbanking.kmm.shared.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BankApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BankApplication)
            modules(sharedModule, appModule, androidModule)
        }
    }
} 
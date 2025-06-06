package com.webbanking.kmm.shared

import com.russhwolf.settings.Settings
import com.russhwolf.settings.ObservableSettings

expect class Platform() {
    val osName: String
    val osVersion: String
    val deviceModel: String
    val density: Int
    val platform: String
    val appVersion: String
} 
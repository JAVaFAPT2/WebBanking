package com.webbanking.kmm.shared

import platform.UIKit.UIDevice
import platform.Foundation.NSBundle

actual class Platform actual constructor() {
    actual val osName: String = UIDevice.currentDevice.systemName()
    actual val osVersion: String = UIDevice.currentDevice.systemVersion
    actual val deviceModel: String = UIDevice.currentDevice.model
    actual val density: Int = 1 // iOS density can be handled differently if needed
    actual val platform: String = osName + " " + osVersion
    actual val appVersion: String = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "1.0.0-ios"
} 
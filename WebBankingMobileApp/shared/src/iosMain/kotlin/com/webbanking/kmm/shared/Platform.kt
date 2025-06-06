package com.webbanking.kmm.shared

import platform.UIKit.UIDevice
import platform.Foundation.NSBundle

actual class Platform actual constructor() {
    actual val platform: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    actual val appVersion: String = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "1.0.0-ios"
} 
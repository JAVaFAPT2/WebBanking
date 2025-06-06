plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.native.cocoapods)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions { jvmTarget = "1.8" }
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.multiplatform.settings.noarg)
            implementation(libs.multiplatform.settings.coroutines)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.cio) // Ktor engine for Android
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin) // Ktor engine for iOS
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "com.webbanking.kmm.shared"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.androidMinSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

// CocoaPods configuration for iOS
iosPod("AFNetworking", "~> 4.0.1") // Example if you need an iOS specific pod, not strictly needed for Ktor alone

kotlin {
    cocoapods {
        summary = "Shared KMM module for WebBanking App"
        homepage = "https://github.com/yourusername/WebBankingMobileApp" // Replace with your repo
        ios.deploymentTarget = "14.1" // Minimum iOS version
        framework {
            baseName = "shared"
            isStatic = true
        }
    }
} 
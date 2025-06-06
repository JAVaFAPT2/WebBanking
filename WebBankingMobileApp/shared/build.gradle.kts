plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.cocoapods)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions { 
                jvmTarget = "1.8"
            }
        }
    }
    jvm()
    
    // iOS targets - these will be skipped on Windows but needed for MacOS
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
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.multiplatform.settings.noarg)
                implementation(libs.multiplatform.settings.coroutines)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }
        val iosMain by creating {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
        val jvmMain by getting {
            dependencies {
                // Add JVM-specific dependencies here if needed
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
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

// CocoaPods configuration - this will be skipped on Windows
kotlin {
    cocoapods {
        summary = "Shared KMM module for WebBanking App"
        homepage = "https://github.com/yourusername/WebBankingMobileApp"
        version = "1.0"
        ios.deploymentTarget = "14.1"
        framework {
            baseName = "shared"
            isStatic = true
        }
        // Add any iOS dependencies here
        // pod("AFNetworking", "~> 4.0.1")  // Uncomment if you need AFNetworking
    }
} 
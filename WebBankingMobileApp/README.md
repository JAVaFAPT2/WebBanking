# WebBanking Mobile App (KMM)

This is a Kotlin Multiplatform Mobile (KMM) application for the WebBanking platform.

## Modules

- `shared`: Contains the common Kotlin code, including business logic, data models, and API interactions. This code is shared between the Android and iOS applications.
- `androidApp`: The Android application, built with Jetpack Compose and consuming the `shared` module.
- `iosApp`: The iOS application, built with SwiftUI and consuming the `shared` module.

## Backend Interaction

The mobile application interacts with the backend services via the `ApiGateway`. Ensure the backend services (defined in the root `docker-compose.yml`) are running.

**Assumed API Gateway Base URL:** `http://localhost:8081` (or your configured gateway URL)

## Setup

1.  Open this `WebBankingMobileApp` directory as a project in Android Studio (with the Kotlin Multiplatform Mobile plugin installed).
2.  Let Gradle sync the project.
3.  You can run the `androidApp` on an Android emulator or device.
4.  For `iosApp`, you'll typically open the `iosApp` sub-project in Xcode. 
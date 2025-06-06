# Testing WebBanking Mobile App with Backend Services

## Prerequisites

1. Docker and Docker Compose installed
2. Android Studio (for Android testing)
3. Xcode (for iOS testing)
4. Android Emulator or physical device
5. iOS Simulator or physical device

## Starting the Backend Services

1. Open a terminal in the root directory (where docker-compose.yml is located)
2. Start all services:
   ```bash
   docker-compose up
   ```
   This will start all services including the API Gateway on port 8080.

## Testing on Android

### Using Android Emulator
- The app is configured to use `10.0.2.2:8080` which is the special alias for your host machine's localhost
- No additional configuration needed
- Run the app from Android Studio

### Using Physical Android Device
1. Find your computer's local IP address:
   - Windows: Run `ipconfig` in cmd
   - Mac/Linux: Run `ifconfig` or `ip addr` in terminal
2. Update the base URL in `shared/src/androidMain/kotlin/com/webbanking/kmm/shared/network/PlatformSpecificNetworkConfig.kt`:
   ```kotlin
   actual val baseUrl: String = "http://YOUR_LOCAL_IP:8080/api"
   ```

## Testing on iOS

### Using iOS Simulator
- The app is configured to use `localhost:8080`
- No additional configuration needed
- Run the app from Xcode

### Using Physical iOS Device
1. Find your computer's local IP address (same as Android)
2. Update the base URL in `shared/src/iosMain/kotlin/com/webbanking/kmm/shared/network/PlatformSpecificNetworkConfig.kt`:
   ```kotlin
   actual val baseUrl: String = "http://YOUR_LOCAL_IP:8080/api"
   ```

## Verifying the Connection

1. Start the backend services using `docker-compose up`
2. Run the mobile app
3. Try to log in with test credentials:
   - Email: user@example.com
   - Password: password123

## Troubleshooting

1. **Cannot connect to backend**
   - Ensure Docker services are running (`docker-compose ps`)
   - Check if API Gateway is accessible:
     - Android Emulator: `http://10.0.2.2:8080/api/health`
     - iOS Simulator: `http://localhost:8080/api/health`
     - Physical devices: `http://YOUR_LOCAL_IP:8080/api/health`

2. **Network Security Issues on Android**
   - The app is configured to allow cleartext traffic (HTTP) for development
   - For production, switch to HTTPS

3. **iOS Network Security**
   - For physical devices, you might need to add your domain to App Transport Security settings in Info.plist

## Development Notes

- The mobile app uses Kotlin Multiplatform Mobile (KMM)
- Shared code is in the `shared` module
- Platform-specific code is in `androidApp` and `iosApp` modules
- Network calls are handled through Ktor client in the shared module
- Authentication token is managed by `AuthTokenManager` in the shared module 
# WebBanking iOS App

This directory is intended for the iOS application that consumes the KMM `shared` module.

## Setup Steps (Manual for iOS Developer)

1.  **Ensure you have CocoaPods installed.** (`sudo gem install cocoapods`)

2.  **Create an Xcode Project:**
    *   If you don't have one, create a new iOS Application project in Xcode (e.g., named `iosApp`) inside this `WebBankingMobileApp/iosApp/` directory.

3.  **Create/Update Podfile:**
    *   Navigate to the `WebBankingMobileApp/iosApp/` directory in your terminal (where your `.xcodeproj` file is).
    *   Run `pod init` if you don't have a `Podfile`.
    *   Add the shared module to your `Podfile`. A placeholder `Podfile` is provided, which you can adapt:
        ```ruby
        platform :ios, '14.1' # Or your desired iOS deployment target

        target 'YourIosAppName' do # Replace YourIosAppName with your actual Xcode target name
          use_frameworks!
          pod 'shared', :path => '../shared'
        end
        ```

4.  **Install Pods:**
    *   In the `WebBankingMobileApp/iosApp/` directory, run `pod install`.
    *   This will generate a `.xcworkspace` file (e.g., `iosApp.xcworkspace`).

5.  **Open Xcode Workspace:**
    *   Close any open Xcode projects and open the newly generated `.xcworkspace` file.
    *   From now on, always use the `.xcworkspace` file to open your iOS project.

6.  **Build the Project:**
    *   Build the project in Xcode (Cmd+B). This ensures the `shared` framework is compiled and linked.

7.  **Use the Shared Module in Swift:**
    *   In your Swift files (e.g., `ContentView.swift`), you can now `import shared` and use the classes and functions defined in the KMM `shared` module.
    *   Example:
        ```swift
        import SwiftUI
        import shared // Import the KMM shared module

        struct ContentView: View {
            var body: some View {
                // Example of using a class from the shared module
                Text(Platform().platform) 
            }
        }
        ```

## Important Notes:

*   The `shared` module's `build.gradle.kts` file contains a `cocoapods` block that defines how the framework is exposed to iOS. You might need to adjust settings like `ios.deploymentTarget` or `framework.baseName` there.
*   Whenever you make changes to the `shared` module, you might need to rebuild the Xcode project (Cmd+B) to see the changes reflected in the iOS app.
*   For networking on iOS simulators with a local backend server, `http://localhost:<port>` usually works if the server is running on the same Mac. For physical devices, the server needs to be accessible on the local network. 
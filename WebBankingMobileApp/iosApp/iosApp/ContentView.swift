//
//  ContentView.swift
//  iosApp (Replace with your actual project name if different)
//
//  Created by YourName on Date. // Replace with actuals
//  Copyright © Year YourCompany. All rights reserved. // Replace with actuals
//

import SwiftUI
import shared // This imports the KMM shared module framework

struct ContentView: View {
    @StateObject private var loginViewModel = LoginViewModelIOS()

    var body: some View {
        NavigationView {
            if loginViewModel.isLoggedIn {
                // Placeholder for a logged-in view
                VStack {
                    Text("Welcome, \(loginViewModel.loggedInUser?.username ?? "User")!")
                    Text("Token: \(loginViewModel.loggedInUser?.token.prefix(10) ?? "")...")
                    Button("Logout") {
                        loginViewModel.logout()
                    }
                }
                .navigationTitle("Dashboard")
            } else {
                LoginView(viewModel: loginViewModel)
            }
        }
        .onAppear {
            loginViewModel.checkInitialAuthState()
        }
    }
}

struct LoginView: View {
    @ObservedObject var viewModel: LoginViewModelIOS

    var body: some View {
        VStack {
            Text("Web Banking iOS")
                .font(.title)
            Text("Shared platform: \(Platform().platform)") // Example usage
            
            Spacer().frame(height: 30)
            
            TextField("Email", text: $viewModel.email)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .padding()
                .autocapitalization(.none)
                .keyboardType(.emailAddress)
            
            SecureField("Password", text: $viewModel.password)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .padding()
            
            if viewModel.isLoading {
                ProgressView()
            } else {
                Button("Login") {
                    viewModel.login()
                }
                .padding()
            }
            
            if let errorMessage = viewModel.errorMessage {
                Text(errorMessage)
                    .foregroundColor(.red)
                    .padding()
            }
            Spacer()
        }
        .navigationTitle("Login")
    }
}

// A simple ObservableObject wrapper for the shared AuthRepository logic
class LoginViewModelIOS: ObservableObject {
    private let authRepository: AuthRepository = AuthRepositoryImpl()
    private let authTokenManager = AuthTokenManager.shared // KMM objects are singletons by default in Swift

    @Published var email = "user@example.com"
    @Published var password = "password123"
    @Published var isLoading = false
    @Published var errorMessage: String? = nil
    @Published var loggedInUser: AuthResponse? = nil
    @Published var isLoggedIn: Bool = false

    init() {
        // Observe token changes to update isLoggedIn state
        // Note: Direct observation of Kotlin Flows in Swift requires a bit more setup
        // or a library like KMP-NativeCoroutines. For simplicity, we check on init and after login/logout.
        checkInitialAuthState()
    }

    func checkInitialAuthState() {
        self.isLoggedIn = authTokenManager.hasToken()
        if self.isLoggedIn {
            // Optionally, you could try to fetch user details here if token exists
            // For now, just update the flag. Actual user data will be set on login.
            print("User is already logged in with a token.")
        }
    }

    func login() {
        isLoading = true
        errorMessage = nil
        // loggedInUser = nil // Keep previous user data until new login succeeds or fails

        let request = LoginRequest(username: email, email: email, password: password)

        authRepository.login(loginRequest: request) { result, error in
            DispatchQueue.main.async {
                self.isLoading = false
                if let successResult = result as? NetworkResultSuccess<AuthResponse> {
                    self.loggedInUser = successResult.data
                    // Token is saved by AuthRepositoryImpl via AuthTokenManager
                    self.isLoggedIn = true
                    print("Login successful for user: \(String(describing: self.loggedInUser?.username))")
                } else if let errorResult = result as? NetworkResultError {
                    self.errorMessage = errorResult.message ?? "Login failed"
                    self.isLoggedIn = false
                } else if error != nil {
                    self.errorMessage = error?.localizedDescription ?? "An unknown error occurred"
                    self.isLoggedIn = false
                }
            }
        }
    }

    func logout() {
        isLoading = true // Optional: show loading indicator
        authRepository.logout { result, error in
            DispatchQueue.main.async {
                self.isLoading = false
                // AuthRepositoryImpl calls AuthTokenManager.clearAuthToken()
                self.loggedInUser = nil
                self.isLoggedIn = false
                self.errorMessage = nil // Clear any previous error messages
                print("Logout completed.")
                if let errorResult = result as? NetworkResultError {
                    self.errorMessage = errorResult.message ?? "Logout failed on server"
                } else if error != nil {
                    self.errorMessage = error?.localizedDescription ?? "An unknown error occurred during logout"
                }
            }
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
} 
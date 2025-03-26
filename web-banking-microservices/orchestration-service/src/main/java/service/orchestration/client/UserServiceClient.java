
package service.orchestrationservice.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.orchestration.dto.UserDTO;
import service.orchestration.dto.UserRegistrationRequest;
import service.orchestration.dto.UserUpdateRequest;

import java.util.List;

/**
 * Feign client for communicating with the user-service
 * Includes circuit breaker fallback implementation
 */
@FeignClient(name = "user-service", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    /**
     * Validate if a user exists and is active
     */
    @GetMapping("/users/validate/{userId}")
    boolean validateUser(@PathVariable("userId") String userId);

    /**
     * Register a new user
     */
    @PostMapping("/users")
    ResponseEntity<UserDTO> registerUser(@RequestBody UserRegistrationRequest request);

    /**
     * Get user by ID
     */
    @GetMapping("/users/{userId}")
    ResponseEntity<UserDTO> getUserById(@PathVariable("userId") String userId);

    /**
     * Get all users
     */
    @GetMapping("/users")
    ResponseEntity<List<UserDTO>> getAllUsers();

    /**
     * Update user information
     */
    @PutMapping("/users/{userId}")
    ResponseEntity<UserDTO> updateUser(
            @PathVariable("userId") String userId,
            @RequestBody UserUpdateRequest request);

    /**
     * Delete a user
     */
    @DeleteMapping("/users/{userId}")
    ResponseEntity<Void> deleteUser(@PathVariable("userId") String userId);

    /**
     * Check if username is available
     */
    @GetMapping("/users/check-username/{username}")
    boolean isUsernameAvailable(@PathVariable("username") String username);

    /**
     * Check if email is available
     */
    @GetMapping("/users/check-email/{email}")
    boolean isEmailAvailable(@PathVariable("email") String email);

    /**
     * Get user by username
     */
    @GetMapping("/users/by-username/{username}")
    ResponseEntity<UserDTO> getUserByUsername(@PathVariable("username") String username);

    /**
     * Search users by criteria
     */
    @GetMapping("/users/search")
    ResponseEntity<List<UserDTO>> searchUsers(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "status", required = false) String status);
}

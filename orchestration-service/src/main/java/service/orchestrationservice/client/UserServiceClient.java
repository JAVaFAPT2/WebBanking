package service.orchestrationservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import service.shared.models.User;  // Ensure this is your correct model class

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);
    private static final String SERVICE_NAME = "user-service";
    private static final String BASE_URL = "/api/users";

    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;

    @Autowired
    public UserServiceClient(WebClient.Builder webClientBuilder,
                             CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.webClient = webClientBuilder
                .baseUrl("lb://" + SERVICE_NAME) // Load balanced URL via service discovery
                .build();
        this.circuitBreaker = circuitBreakerFactory.create("userService");
    }

    /**
     * Get user by ID.
     */
    public Mono<User> getUserById(UUID userId) {
        log.debug("Getting user by ID: {}", userId);
        return Mono.defer(() ->
                Mono.just(circuitBreaker.run(
                        () -> webClient.get()
                                .uri(BASE_URL + "/{id}", userId)
                                .retrieve()
                                .onStatus(
                                        response -> response.value() == HttpStatus.NOT_FOUND.value(),
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("User not found: " + error)))
                                )
                                .onStatus(
                                        response -> response.value() >= 400,
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("Error fetching user: " + error)))
                                )
                                .bodyToMono(User.class)
                                .timeout(Duration.ofSeconds(5))
                                .block(),
                        throwable -> this.handleUserServiceFailure(throwable, "get-user")
                ))
        );
    }

    /**
     * Get users by criteria.
     */
    public Mono<List<User>> getUsersByCriteria(Map<String, String> criteria) {
        log.debug("Getting users by criteria: {}", criteria);
        return Mono.defer(() ->
                Mono.just(circuitBreaker.run(
                        () -> webClient.get()
                                .uri(uriBuilder -> {
                                    uriBuilder.path(BASE_URL);
                                    criteria.forEach(uriBuilder::queryParam);
                                    return uriBuilder.build();
                                })
                                .retrieve()
                                .onStatus(
                                        response -> response.value() >= 400,
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("Error fetching users: " + error)))
                                )
                                .bodyToFlux(User.class)
                                .collectList()
                                .timeout(Duration.ofSeconds(10))
                                .block(),
                        throwable -> this.handleUserServiceFailure(throwable, "get-users")
                ))
        );
    }

    /**
     * Authenticate user.
     */
    public Mono<User> authenticateUser(String username, String password) {
        log.debug("Authenticating user: {}", username);
        Map<String, String> credentials = Map.of("username", username, "password", password);
        return Mono.defer(() ->
                Mono.just(circuitBreaker.run(
                        () -> webClient.post()
                                .uri(BASE_URL + "/authenticate")
                                .bodyValue(credentials)
                                .retrieve()
                                .onStatus(
                                        response -> response.value() == HttpStatus.UNAUTHORIZED.value(),
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("Invalid credentials: " + error)))
                                )
                                .onStatus(
                                        response -> response.value() >= 400,
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("Authentication error: " + error)))
                                )
                                .bodyToMono(User.class)
                                .timeout(Duration.ofSeconds(5))
                                .block(),
                        throwable -> this.handleUserServiceFailure(throwable, "authenticate")
                ))
        );
    }

    /**
     * Create a new user.
     */
    public Mono<User> createUser(User user) {
        log.debug("Creating user: {}", user.getUsername());
        return Mono.defer(() ->
                Mono.just(circuitBreaker.run(
                        () -> webClient.post()
                                .uri(BASE_URL)
                                .bodyValue(user)
                                .retrieve()
                                .onStatus(
                                        response -> response.value() == HttpStatus.CONFLICT.value(),
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("User already exists: " + error)))
                                )
                                .onStatus(
                                        response -> response.value() >= 400,
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("Error creating user: " + error)))
                                )
                                .bodyToMono(User.class)
                                .timeout(Duration.ofSeconds(5))
                                .block(),
                        throwable -> this.handleUserServiceFailure(throwable, "create-user")
                ))
        );
    }

    /**
     * Update an existing user.
     */
    public Mono<User> updateUser(UUID userId, User user) {
        log.debug("Updating user: {}", userId);
        return Mono.defer(() ->
                Mono.just(circuitBreaker.run(
                        () -> webClient.put()
                                .uri(BASE_URL + "/{id}", userId)
                                .bodyValue(user)
                                .retrieve()
                                .onStatus(
                                        response -> response.value() == HttpStatus.NOT_FOUND.value(),
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("User not found: " + error)))
                                )
                                .onStatus(
                                        response -> response.value() >= 400,
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("Error updating user: " + error)))
                                )
                                .bodyToMono(User.class)
                                .timeout(Duration.ofSeconds(5))
                                .block(),
                        throwable -> this.handleUserServiceFailure(throwable, "update-user")
                ))
        );
    }

    /**
     * Delete a user.
     */
    public Mono<Void> deleteUser(UUID userId) {
        log.debug("Deleting user: {}", userId);
        return Mono.defer(() ->
                Mono.just(circuitBreaker.run(
                        () -> webClient.delete()
                                .uri(BASE_URL + "/{id}", userId)
                                .retrieve()
                                .onStatus(
                                        response -> response.value() == HttpStatus.NOT_FOUND.value(),
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("User not found: " + error)))
                                )
                                .onStatus(
                                        response -> response.value() >= 400,
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("Error deleting user: " + error)))
                                )
                                .bodyToMono(Void.class)
                                .timeout(Duration.ofSeconds(5))
                                .block(),
                        throwable -> this.handleUserServiceFailure(throwable, "delete-user")
                ))
        ).then();
    }

    /**
     * Verify if the user exists and has the required role.
     */
    public Mono<Boolean> verifyUserRole(UUID userId, String requiredRole) {
        log.debug("Verifying role for user: {}", userId);
        return getUserById(userId)
                .map(user -> user.getRole().equals(requiredRole))
                .onErrorReturn(false);
    }

    /**
     * Handle failures in user service calls.
     */
    @SuppressWarnings("unchecked")
    private <T> T handleUserServiceFailure(Throwable throwable, String operation) {
        log.error("User service failure during {}: {}", operation, throwable.getMessage());
        if ("get-users".equals(operation)) {
            return (T) Collections.emptyList();
        } else if ("authenticate".equals(operation) ||
                "get-user".equals(operation) ||
                "create-user".equals(operation) ||
                "update-user".equals(operation)) {
            throw new RuntimeException("User service unavailable", throwable);
        } else {
            return null;
        }
    }
}

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
import service.shared.models.Account;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class AccountServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceClient.class);
    private static final String SERVICE_NAME = "account-service";
    private static final String BASE_URL = "/api/accounts";

    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;

    @Autowired
    public AccountServiceClient(WebClient.Builder webClientBuilder,
                                CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.webClient = webClientBuilder
                .baseUrl("lb://" + SERVICE_NAME) // Load balanced URL via service discovery
                .build();
        this.circuitBreaker = circuitBreakerFactory.create("accountService");
    }

    /**
     * Get account by ID.
     */
    public Mono<Account> getAccountById(UUID accountId) {
        log.debug("Getting account by ID: {}", accountId);
        return Mono.defer(() ->
                Mono.just(circuitBreaker.run(
                        () -> webClient.get()
                                .uri(BASE_URL + "/{id}", accountId)
                                .retrieve()
                                .onStatus(
                                        response -> response.value() == HttpStatus.NOT_FOUND.value(),
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("Account not found: " + error)))
                                )
                                .onStatus(
                                        response -> response.value() >= 400,
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("Error fetching account: " + error)))
                                )
                                .bodyToMono(Account.class)
                                .timeout(Duration.ofSeconds(5))
                                .block(),
                        throwable -> this.handleAccountServiceFailure(throwable, "get-account")
                ))
        );
    }

    /**
     * Get accounts by criteria.
     */
    public Mono<List<Account>> getAccountsByCriteria(Map<String, String> criteria) {
        log.debug("Getting accounts by criteria: {}", criteria);
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
                                                .flatMap(error -> Mono.error(new RuntimeException("Error fetching accounts: " + error)))
                                )
                                .bodyToFlux(Account.class)
                                .collectList()
                                .timeout(Duration.ofSeconds(10))
                                .block(),
                        throwable -> this.handleAccountServiceFailure(throwable, "get-accounts")
                ))
        );
    }

    /**
     * Create a new account.
     */
    public Mono<Account> createAccount(Account account) {
        log.debug("Creating account");
        return Mono.defer(() ->
                Mono.just(circuitBreaker.run(
                        () -> webClient.post()
                                .uri(BASE_URL)
                                .bodyValue(account)
                                .retrieve()
                                .onStatus(
                                        response -> response.value() == HttpStatus.CONFLICT.value(),
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("Account already exists: " + error)))
                                )
                                .onStatus(
                                        response -> response.value() >= 400,
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("Error creating account: " + error)))
                                )
                                .bodyToMono(Account.class)
                                .timeout(Duration.ofSeconds(5))
                                .block(),
                        throwable -> this.handleAccountServiceFailure(throwable, "create-account")
                ))
        );
    }

    /**
     * Update an existing account.
     */
    public Mono<Account> updateAccount(UUID accountId, Account account) {
        log.debug("Updating account: {}", accountId);
        return Mono.defer(() ->
                Mono.just(circuitBreaker.run(
                        () -> webClient.put()
                                .uri(BASE_URL + "/{id}", accountId)
                                .bodyValue(account)
                                .retrieve()
                                .onStatus(
                                        response -> response.value() == HttpStatus.NOT_FOUND.value(),
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("Account not found: " + error)))
                                )
                                .onStatus(
                                        response -> response.value() >= 400,
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("Error updating account: " + error)))
                                )
                                .bodyToMono(Account.class)
                                .timeout(Duration.ofSeconds(5))
                                .block(),
                        throwable -> this.handleAccountServiceFailure(throwable, "update-account")
                ))
        );
    }

    /**
     * Delete an account.
     */
    public Mono<Void> deleteAccount(UUID accountId) {
        log.debug("Deleting account: {}", accountId);
        return Mono.defer(() ->
                Mono.just(circuitBreaker.run(
                        () -> webClient.delete()
                                .uri(BASE_URL + "/{id}", accountId)
                                .retrieve()
                                .onStatus(
                                        response -> response.value() == HttpStatus.NOT_FOUND.value(),
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("Account not found: " + error)))
                                )
                                .onStatus(
                                        response -> response.value() >= 400,
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("Error deleting account: " + error)))
                                )
                                .bodyToMono(Void.class)
                                .timeout(Duration.ofSeconds(5))
                                .block(),
                        throwable -> this.handleAccountServiceFailure(throwable, "delete-account")
                ))
        ).then();
    }

    /**
     * Get account balance.
     */
    public Mono<Double> getAccountBalance(UUID accountId) {
        log.debug("Getting balance for account: {}", accountId);
        return Mono.defer(() ->
                Mono.just(circuitBreaker.run(
                        () -> webClient.get()
                                .uri(BASE_URL + "/{id}/balance", accountId)
                                .retrieve()
                                .onStatus(
                                        response -> response.value() == HttpStatus.NOT_FOUND.value(),
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("Account not found: " + error)))
                                )
                                .onStatus(
                                        response -> response.value() >= 400,
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("Error getting balance: " + error)))
                                )
                                .bodyToMono(Double.class)
                                .timeout(Duration.ofSeconds(5))
                                .block(),
                        throwable -> this.handleAccountServiceFailure(throwable, "get-balance")
                ))
        );
    }

    /**
     * Handle failures in account service calls.
     */
    @SuppressWarnings("unchecked")
    private <T> T handleAccountServiceFailure(Throwable throwable, String operation) {
        log.error("Account service failure during {}: {}", operation, throwable.getMessage());
        if ("get-accounts".equals(operation)) {
            return (T) Collections.emptyList();
        } else if ("get-account".equals(operation) ||
                "create-account".equals(operation) ||
                "update-account".equals(operation)) {
            throw new RuntimeException("Account service unavailable", throwable);
        } else {
            return null;
        }
    }
}
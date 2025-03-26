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
import service.orchestrationservice.model.TransactionRequest;
import service.shared.models.Transaction;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class TransactionServiceClient {

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceClient.class);
    private static final String SERVICE_NAME = "transaction-service";
    private static final String BASE_URL = "/api/transactions";

    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;

    @Autowired
    public TransactionServiceClient(WebClient.Builder webClientBuilder,
                                    CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.webClient = webClientBuilder
                .baseUrl("lb://" + SERVICE_NAME) // Load balanced URL via service discovery
                .build();
        this.circuitBreaker = circuitBreakerFactory.create("transactionService");
    }

    /**
     * Record a new transaction.
     */
    public Mono<Transaction> recordTransaction(TransactionRequest transactionRequest) {
        log.debug("Recording transaction");
        return Mono.defer(() ->
                Mono.just(circuitBreaker.run(
                        () -> webClient.post()
                                .uri(BASE_URL)
                                .bodyValue(transactionRequest)
                                .retrieve()
                                .onStatus(
                                        response -> response.value() == HttpStatus.CONFLICT.value(),
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("Transaction already exists: " + error)))
                                )
                                .onStatus(
                                        response -> response.value() >= 400,
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("Error recording transaction: " + error)))
                                )
                                .bodyToMono(Transaction.class)
                                .timeout(Duration.ofSeconds(5))
                                .block(),
                        throwable -> this.handleTransactionServiceFailure(throwable, "record-transaction")
                ))
        );
    }

    /**
     * Get transaction by ID.
     */
    public Mono<Transaction> getTransactionById(UUID transactionId) {
        log.debug("Getting transaction by ID: {}", transactionId);
        return Mono.defer(() ->
                Mono.just(circuitBreaker.run(
                        () -> webClient.get()
                                .uri(BASE_URL + "/{id}", transactionId)
                                .retrieve()
                                .onStatus(
                                        response -> response.value() == HttpStatus.NOT_FOUND.value(),
                                        response -> Mono.error(new RuntimeException("Transaction not found"))
                                )
                                .onStatus(
                                        response -> response.value() >= 400,
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("Error fetching transaction: " + error)))
                                )
                                .bodyToMono(Transaction.class)
                                .timeout(Duration.ofSeconds(5))
                                .block(),
                        throwable -> this.handleTransactionServiceFailure(throwable, "get-transaction")
                ))
        );
    }

    /**
     * Get transactions by criteria.
     */
    public Mono<List<Transaction>> getTransactionsByCriteria(Map<String, String> criteria) {
        log.debug("Getting transactions by criteria: {}", criteria);
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
                                                .flatMap(error -> Mono.error(new RuntimeException("Error fetching transactions: " + error)))
                                )
                                .bodyToFlux(Transaction.class)
                                .collectList()
                                .timeout(Duration.ofSeconds(10))
                                .block(),
                        throwable -> this.handleTransactionServiceFailure(throwable, "get-transactions")
                ))
        );
    }

    /**
     * Handle failures in transaction service calls.
     */
    @SuppressWarnings("unchecked")
    private <T> T handleTransactionServiceFailure(Throwable throwable, String operation) {
        log.error("Transaction service failure during {}: {}", operation, throwable.getMessage());
        if ("get-transactions".equals(operation)) {
            return (T) Collections.emptyList();
        } else if ("record-transaction".equals(operation) ||
                "get-transaction".equals(operation)) {
            throw new RuntimeException("Transaction service unavailable", throwable);
        } else {
            return null;
        }
    }
}
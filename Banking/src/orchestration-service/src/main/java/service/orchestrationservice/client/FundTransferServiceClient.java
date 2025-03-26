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
import service.orchestrationservice.model.TransferRequest;
import service.shared.models.Transfer;

import java.time.Duration;
import java.util.UUID;

@Component
public class FundTransferServiceClient {

    private static final Logger log = LoggerFactory.getLogger(FundTransferServiceClient.class);
    private static final String SERVICE_NAME = "fund-transfer-service";
    private static final String BASE_URL = "/api/transfers";

    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;

    @Autowired
    public FundTransferServiceClient(WebClient.Builder webClientBuilder,
                                     CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.webClient = webClientBuilder
                .baseUrl("lb://" + SERVICE_NAME) // Load balanced URL via service discovery
                .build();
        this.circuitBreaker = circuitBreakerFactory.create("fundTransferService");
    }

    /**
     * Initiate a new fund transfer.
     */
    public Mono<Transfer> initiateTransfer(TransferRequest transferRequest) {
        log.debug("Initiating fund transfer");
        return Mono.defer(() ->
                Mono.just(circuitBreaker.run(
                        () -> webClient.post()
                                .uri(BASE_URL)
                                .bodyValue(transferRequest)
                                .retrieve()
                                .onStatus(
                                        response -> response.value() == HttpStatus.CONFLICT.value(),
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("Transfer already exists: " + error)))
                                )
                                .onStatus(
                                        response -> response.value() >= 400,
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("Error initiating transfer: " + error)))
                                )
                                .bodyToMono(Transfer.class)
                                .timeout(Duration.ofSeconds(5))
                                .block(),
                        throwable -> this.handleFundTransferServiceFailure(throwable, "initiate-transfer")
                ))
        );
    }

    /**
     * Get transfer by ID.
     */
    public Mono<Transfer> getTransferById(UUID transferId) {
        log.debug("Getting transfer by ID: {}", transferId);
        return Mono.defer(() ->
                Mono.just(circuitBreaker.run(
                        () -> webClient.get()
                                .uri(BASE_URL + "/{id}", transferId)
                                .retrieve()
                                .onStatus(
                                        response -> response.value() == HttpStatus.NOT_FOUND.value(),
                                        response -> Mono.error(new RuntimeException("Transfer not found"))
                                )
                                .onStatus(
                                        response -> response.value() >= 400,
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("Error fetching transfer: " + error)))
                                )
                                .bodyToMono(Transfer.class)
                                .timeout(Duration.ofSeconds(5))
                                .block(),
                        throwable -> this.handleFundTransferServiceFailure(throwable, "get-transfer")
                ))
        );
    }

    /**
     * Update an existing transfer.
     */
    public Mono<Transfer> updateTransfer(UUID transferId, Transfer transfer) {
        log.debug("Updating transfer: {}", transferId);
        return Mono.defer(() ->
                Mono.just(circuitBreaker.run(
                        () -> webClient.put()
                                .uri(BASE_URL + "/{id}", transferId)
                                .bodyValue(transfer)
                                .retrieve()
                                .onStatus(
                                        response -> response.value() == HttpStatus.NOT_FOUND.value(),
                                        response -> Mono.error(new RuntimeException("Transfer not found"))
                                )
                                .onStatus(
                                        response -> response.value() >= 400,
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("Error updating transfer: " + error)))
                                )
                                .bodyToMono(Transfer.class)
                                .timeout(Duration.ofSeconds(5))
                                .block(),
                        throwable -> this.handleFundTransferServiceFailure(throwable, "update-transfer")
                ))
        );
    }

    /**
     * Cancel a transfer.
     */
    public Mono<Void> cancelTransfer(UUID transferId) {
        log.debug("Canceling transfer: {}", transferId);
        return Mono.defer(() ->
                Mono.just(circuitBreaker.run(
                        () -> webClient.delete()
                                .uri(BASE_URL + "/{id}", transferId)
                                .retrieve()
                                .onStatus(
                                        response -> response.value() == HttpStatus.NOT_FOUND.value(),
                                        response -> Mono.error(new RuntimeException("Transfer not found"))
                                )
                                .onStatus(
                                        response -> response.value() >= 400,
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("Error canceling transfer: " + error)))
                                )
                                .bodyToMono(Void.class)
                                .timeout(Duration.ofSeconds(5))
                                .block(),
                        throwable -> this.handleFundTransferServiceFailure(throwable, "cancel-transfer")
                ))
        ).then();
    }

    /**
     * Handle failures in fund transfer service calls.
     */
    @SuppressWarnings("unchecked")
    private <T> T handleFundTransferServiceFailure(Throwable throwable, String operation) {
        log.error("Fund transfer service failure during {}: {}", operation, throwable.getMessage());
        if ("get-transfer".equals(operation)) {
            return null;
        } else if ("initiate-transfer".equals(operation) ||
                "update-transfer".equals(operation)) {
            throw new RuntimeException("Fund transfer service unavailable", throwable);
        } else {
            return null;
        }
    }
}
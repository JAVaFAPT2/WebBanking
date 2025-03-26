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
import service.orchestrationservice.model.NotificationRequest;
import service.shared.models.Notification;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class NotificationServiceClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceClient.class);
    private static final String SERVICE_NAME = "notification-service";
    private static final String BASE_URL = "/api/notifications";

    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;

    @Autowired
    public NotificationServiceClient(WebClient.Builder webClientBuilder,
                                     CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.webClient = webClientBuilder
                .baseUrl("lb://" + SERVICE_NAME) // Load balanced URL via service discovery
                .build();
        this.circuitBreaker = circuitBreakerFactory.create("notificationService");
    }

    /**
     * Send a notification to a user.
     */
    public Mono<Void> sendNotification(NotificationRequest notificationRequest) {
        log.debug("Sending notification to user: {}", notificationRequest.getUserId());
        return Mono.defer(() ->
                Mono.just(circuitBreaker.run(
                        () -> webClient.post()
                                .uri(BASE_URL)
                                .bodyValue(notificationRequest)
                                .retrieve()
                                .onStatus(
                                        response -> response.value() >= 400,
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("Error sending notification: " + error)))
                                )
                                .bodyToMono(Void.class)
                                .timeout(Duration.ofSeconds(5))
                                .block(),
                        throwable -> this.handleNotificationServiceFailure(throwable, "send-notification")
                ))
        );
    }

    /**
     * Get notifications for a user.
     */
    public Mono<List<Notification>> getNotificationsByUserId(UUID userId) {
        log.debug("Getting notifications for user: {}", userId);
        return Mono.defer(() ->
                Mono.just(circuitBreaker.run(
                        () -> webClient.get()
                                .uri(BASE_URL + "/user/{userId}", userId)
                                .retrieve()
                                .onStatus(
                                        response -> response.value() == HttpStatus.NOT_FOUND.value(),
                                        response -> Mono.error(new RuntimeException("Notifications not found"))
                                )
                                .onStatus(
                                        response -> response.value() >= 400,
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("Error fetching notifications: " + error)))
                                )
                                .bodyToFlux(Notification.class)
                                .collectList()
                                .timeout(Duration.ofSeconds(5))
                                .block(),
                        throwable -> this.handleNotificationServiceFailure(throwable, "get-notifications")
                ))
        );
    }

    /**
     * Handle failures in notification service calls.
     */
    @SuppressWarnings("unchecked")
    private <T> T handleNotificationServiceFailure(Throwable throwable, String operation) {
        log.error("Notification service failure during {}: {}", operation, throwable.getMessage());
        if ("get-notifications".equals(operation)) {
            return (T) Collections.emptyList();
        } else if ("send-notification".equals(operation)) {
            throw new RuntimeException("Notification service unavailable", throwable);
        } else {
            return null;
        }
    }
}
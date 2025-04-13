package service.messagebroker.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import service.messagebroker.models.KafkaMessage;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * Producer service for sending messages to Kafka topics
 * Provides methods for different types of banking events
 */
@Component
public class MessageProducer {
    private static final Logger logger = LoggerFactory.getLogger(MessageProducer.class);

    private final KafkaTemplate<String, KafkaMessage> kafkaTemplate;

    @Autowired
    public MessageProducer(KafkaTemplate<String, KafkaMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Send a message to a specified Kafka topic
     *
     * @param topic   The Kafka topic to send the message to
     * @param key     The message key (used for partitioning)
     * @param message The message to send
     * @return CompletableFuture of the send result
     */
    public CompletableFuture<SendResult<String, KafkaMessage>> sendMessage(String topic, String key, KafkaMessage message) {
        logger.info("Sending message to topic {}: {}", topic, message);

        CompletableFuture<SendResult<String, KafkaMessage>> future = kafkaTemplate.send(topic, key, message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Message sent successfully to topic {}: offset=[{}]",
                        topic, result.getRecordMetadata().offset());
            } else {
                logger.error("Failed to send message to topic {}", topic, ex);
            }
        });

        return future;
    }

    /**
     * Send a transaction event
     *
     * @param accountId     The account ID associated with the transaction
     * @param transactionId The unique transaction identifier
     * @param amount        The transaction amount
     * @param description   Description of the transaction
     * @param isInternational Whether the transaction is international
     * @return CompletableFuture of the send result
     */
    public CompletableFuture<SendResult<String, KafkaMessage>> sendTransactionEvent(
            String accountId, String transactionId, double amount, String description, boolean isInternational) {

        KafkaMessage message = new KafkaMessage(
                KafkaMessage.MessageType.TRANSACTION,
                "transaction-service",
                accountId,
                "TRANSACTION_CREATED"
        );

        Map<String, Object> payload = new HashMap<>();
        payload.put("transactionId", transactionId);
        payload.put("accountId", accountId);
        payload.put("amount", amount);
        payload.put("description", description);
        payload.put("timestamp", java.time.LocalDateTime.now().toString());
        payload.put("isInternational", isInternational);

        message.setPayload(payload);

        // Set appropriate priority based on amount
        if (amount > 10000) {
            message.setPriority(KafkaMessage.Priority.HIGH);
        }

        return sendMessage("transaction-topic", accountId, message);
    }

    /**
     * Send a notification event
     *
     * @param userId      The user ID to notify
     * @param title       The notification title
     * @param content     The notification content
     * @param notificationType The type of notification (e.g., ALERT, INFO)
     * @param priority    The notification priority
     * @return CompletableFuture of the send result
     */
    public CompletableFuture<SendResult<String, KafkaMessage>> sendNotificationEvent(
            String userId, String title, String content, String notificationType, KafkaMessage.Priority priority) {

        KafkaMessage message = new KafkaMessage(
                KafkaMessage.MessageType.NOTIFICATION,
                "notification-service",
                userId,
                "NOTIFICATION_CREATED"
        );

        Map<String, Object> payload = new HashMap<>();
        payload.put("title", title);
        payload.put("content", content);
        payload.put("timestamp", java.time.LocalDateTime.now().toString());
        payload.put("notificationType", notificationType);
        payload.put("read", false);

        message.setPayload(payload);
        message.setPriority(priority);

        return sendMessage("notification-topic", userId, message);
    }

    /**
     * Send a user activity event
     *
     * @param userId    The user ID performing the activity
     * @param activity  The activity description
     * @param details   Additional activity details
     * @return CompletableFuture of the send result
     */
    public CompletableFuture<SendResult<String, KafkaMessage>> sendUserActivityEvent(
            String userId, String activity, Map<String, Object> details) {

        KafkaMessage message = new KafkaMessage(
                KafkaMessage.MessageType.USER_ACTIVITY,
                "user-service",
                userId,
                activity
        );

        Map<String, Object> payload = new HashMap<>(details);
        payload.put("timestamp", java.time.LocalDateTime.now().toString());
        payload.put("userId", userId);
        payload.put("ipAddress", details.getOrDefault("ipAddress", "unknown"));
        payload.put("userAgent", details.getOrDefault("userAgent", "unknown"));

        message.setPayload(payload);
        message.setPriority(KafkaMessage.Priority.LOW);

        return sendMessage("user-activity-topic", userId, message);
    }

    /**
     * Send a fraud alert event
     *
     * @param accountId     The account ID associated with the potential fraud
     * @param transactionId The transaction ID that triggered the alert
     * @param fraudType     The type of fraud detected
     * @param details       Additional details about the fraud detection
     * @return CompletableFuture of the send result
     */
    public CompletableFuture<SendResult<String, KafkaMessage>> sendFraudAlertEvent(
            String accountId, String transactionId, String fraudType, Map<String, Object> details) {

        KafkaMessage message = new KafkaMessage(
                KafkaMessage.MessageType.SYSTEM_ALERT,
                "fraud-detection-service",
                accountId,
                "FRAUD_DETECTED"
        );

        Map<String, Object> payload = new HashMap<>(details);
        payload.put("transactionId", transactionId);
        payload.put("accountId", accountId);
        payload.put("fraudType", fraudType);
        payload.put("detectionTime", java.time.LocalDateTime.now().toString());
        payload.put("severity", "HIGH");

        message.setPayload(payload);
        message.setPriority(KafkaMessage.Priority.CRITICAL);

        // Send to fraud-alert topic
        return sendMessage("fraud-alert-topic", accountId, message);
    }

    /**
     * Send a message with custom callback handlers
     *
     * @param topic      The Kafka topic to send the message to
     * @param key        The message key
     * @param message    The message to send
     * @param onSuccess  Callback for successful send
     * @param onFailure  Callback for failed send
     */
    public void sendMessageWithCallbacks(
            String topic,
            String key,
            KafkaMessage message,
            BiConsumer<SendResult<String, KafkaMessage>, Void> onSuccess,
            BiConsumer<Void, Throwable> onFailure) {

        CompletableFuture<SendResult<String, KafkaMessage>> future = kafkaTemplate.send(topic, key, message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                onSuccess.accept(result, null);
            } else {
                onFailure.accept(null, ex);
            }
        });
    }
}

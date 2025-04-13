package service.messagebroker.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import service.messagebroker.models.KafkaMessage;
import service.messagebroker.producer.MessageProducer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Kafka message listener for processing incoming messages from various topics
 * Handles different types of messages and performs appropriate actions
 */
@Component
public class MessageListener {
    private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);

    private final MessageProducer messageProducer;
    private final ExecutorService executorService;

    @Autowired
    public MessageListener(MessageProducer messageProducer) {
        this.messageProducer = messageProducer;
        // Create a thread pool for handling time-consuming operations
        this.executorService = Executors.newFixedThreadPool(5);
    }

    /**
     * Listen for transaction messages
     */
    @KafkaListener(
            topics = "${spring.kafka.topics.transaction}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenTransactions(
            @Payload KafkaMessage message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        try {
            logger.info("Received transaction message: {} from topic: {}, partition: {}, offset: {}",
                    message.getMessageId(), topic, partition, offset);

            // Process the transaction message
            processTransactionMessage(message);

            // Acknowledge the message after successful processing
            acknowledgment.acknowledge();

            logger.info("Transaction message processed successfully: {}", message.getMessageId());
        } catch (Exception e) {
            logger.error("Error processing transaction message: " + message.getMessageId(), e);
            // In case of error, we might want to retry or send to a dead letter queue
            // For now, we'll still acknowledge to prevent getting stuck on a bad message
            acknowledgment.acknowledge();
        }
    }

    /**
     * Listen for notification messages
     */
    @KafkaListener(
            topics = "${spring.kafka.topics.notification}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenNotifications(
            @Payload KafkaMessage message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment
    ) {
        try {
            logger.info("Received notification message: {}", message.getMessageId());

            // Process the notification message
            processNotificationMessage(message);

            // Acknowledge the message
            acknowledgment.acknowledge();

            logger.info("Notification message processed successfully: {}", message.getMessageId());
        } catch (Exception e) {
            logger.error("Error processing notification message: " + message.getMessageId(), e);
            acknowledgment.acknowledge();
        }
    }

    /**
     * Listen for user activity messages
     */
    @KafkaListener(
            topics = "${spring.kafka.topics.user-activity}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenUserActivity(
            @Payload KafkaMessage message,
            Acknowledgment acknowledgment
    ) {
        try {
            logger.info("Received user activity message: {}", message.getMessageId());

            // Process the user activity message
            processUserActivityMessage(message);

            // Acknowledge the message
            acknowledgment.acknowledge();

            logger.info("User activity message processed successfully: {}", message.getMessageId());
        } catch (Exception e) {
            logger.error("Error processing user activity message: " + message.getMessageId(), e);
            acknowledgment.acknowledge();
        }
    }

    /**
     * Listen for fraud alert messages
     */
    @KafkaListener(
            topics = "${spring.kafka.topics.fraud-alert}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenFraudAlerts(
            @Payload KafkaMessage message,
            Acknowledgment acknowledgment
    ) {
        try {
            logger.info("Received fraud alert message: {}", message.getMessageId());

            // Process the fraud alert message - this might be high priority
            processFraudAlertMessage(message);

            // Acknowledge the message
            acknowledgment.acknowledge();

            logger.info("Fraud alert message processed successfully: {}", message.getMessageId());
        } catch (Exception e) {
            logger.error("Error processing fraud alert message: " + message.getMessageId(), e);
            acknowledgment.acknowledge();
        }
    }

    /**
     * Process transaction messages
     */
    private void processTransactionMessage(KafkaMessage message) {
        Map<String, Object> payload = message.getPayload();
        String action = message.getAction();

        // Handle different transaction actions
        switch (action) {
            case "TRANSACTION_CREATED":
                handleNewTransaction(payload);
                break;
            case "TRANSACTION_UPDATED":
                handleUpdatedTransaction(payload);
                break;
            case "TRANSACTION_FAILED":
                handleFailedTransaction(payload);
                break;
            default:
                logger.warn("Unknown transaction action: {}", action);
        }
    }

    /**
     * Process notification messages
     */
    private void processNotificationMessage(KafkaMessage message) {
        Map<String, Object> payload = message.getPayload();

        // Check notification priority and handle accordingly
        KafkaMessage.Priority priority = message.getPriority();
        if (priority == KafkaMessage.Priority.HIGH || priority == KafkaMessage.Priority.CRITICAL) {
            // For high priority notifications, we might want to send push notifications
            sendPushNotification(payload);
        } else {
            // For regular notifications, just store them for retrieval
            storeNotification(payload);
        }
    }

    /**
     * Process user activity messages
     */
    private void processUserActivityMessage(KafkaMessage message) {
        // User activity processing might be less time-sensitive, so we can use the thread pool
        executorService.submit(() -> {
            try {
                Map<String, Object> payload = message.getPayload();
                String activity = message.getAction();

                // Log user activity for analytics
                logUserActivity(payload, activity);

                // Check for suspicious activity patterns
                if (isSuspiciousActivity(payload, activity)) {
                    reportSuspiciousActivity(payload, activity);
                }
            } catch (Exception e) {
                logger.error("Error in async processing of user activity", e);
            }
        });
    }

    /**
     * Process fraud alert messages
     */
    private void processFraudAlertMessage(KafkaMessage message) {
        Map<String, Object> payload = message.getPayload();

        // Fraud alerts are critical and need immediate attention
        String accountId = message.getSubject();
        String fraudType = (String) payload.getOrDefault("fraudType", "UNKNOWN");

        // Take immediate action like freezing the account
        freezeAccountIfNeeded(accountId, fraudType, payload);

        // Notify security team
        notifySecurityTeam(accountId, fraudType, payload);

        // Send urgent notification to the user
        notifyUserOfFraudAlert(accountId, fraudType);
    }

    // Helper methods for transaction processing

    private void handleNewTransaction(Map<String, Object> payload) {
        String transactionId = (String) payload.get("transactionId");
        logger.info("Processing new transaction: {}", transactionId);

        // Here you would implement business logic for new transactions
        // For example, updating account balances, checking for fraud, etc.
    }

    private void handleUpdatedTransaction(Map<String, Object> payload) {
        String transactionId = (String) payload.get("transactionId");
        logger.info("Processing updated transaction: {}", transactionId);

        // Logic for handling transaction updates
    }

    private void handleFailedTransaction(Map<String, Object> payload) {
        String transactionId = (String) payload.get("transactionId");
        String reason = (String) payload.getOrDefault("failureReason", "Unknown");
        logger.info("Processing failed transaction: {}, reason: {}", transactionId, reason);

        // Logic for handling failed transactions
        // This might include notifying the user, reversing holds, etc.
    }

    // Helper methods for notification processing

    private void sendPushNotification(Map<String, Object> payload) {
        String title = (String) payload.get("title");
        String content = (String) payload.get("content");
        logger.info("Sending push notification: {}", title);

        // Logic to send push notification to user's device
        // This would typically involve calling an external push notification service
    }

    private void storeNotification(Map<String, Object> payload) {
        logger.info("Storing notification for later retrieval");

        // Logic to store the notification in a database
    }

    // Helper methods for user activity processing

    private void logUserActivity(Map<String, Object> payload, String activity) {
        String userId = (String) payload.get("userId");
        logger.info("Logging user activity for user {}: {}", userId, activity);

        // Logic to log user activity for analytics purposes
    }

    private boolean isSuspiciousActivity(Map<String, Object> payload, String activity) {
        // Implement logic to detect suspicious activity patterns
        // This could involve checking for unusual login locations, rapid account changes, etc.
        return false; // Placeholder
    }

    private void reportSuspiciousActivity(Map<String, Object> payload, String activity) {
        String userId = (String) payload.get("userId");
        logger.warn("Suspicious activity detected for user {}: {}", userId, activity);

        // Logic to report suspicious activity
        // This might involve creating a fraud alert
        Map<String, Object> alertDetails = new HashMap<>(payload);
        alertDetails.put("detectedActivity", activity);
        alertDetails.put("suspiciousReason", "Unusual pattern detected");

        messageProducer.sendFraudAlertEvent(
                userId,
                "ACTIVITY_" + System.currentTimeMillis(),
                "SUSPICIOUS_ACTIVITY",
                alertDetails
        );
    }

    // Helper methods for fraud alert processing

    private void freezeAccountIfNeeded(String accountId, String fraudType, Map<String, Object> details) {
        // Logic to determine if account should be frozen
        if ("UNUSUAL_LOCATION".equals(fraudType) || "RAPID_SUCCESSION".equals(fraudType)) {
            logger.warn("Freezing account {} due to fraud type: {}", accountId, fraudType);

            // Logic to freeze the account
            // This would typically involve calling an account service API
        }
    }

    private void notifySecurityTeam(String accountId, String fraudType, Map<String, Object> details) {
        logger.info("Notifying security team about fraud alert for account {}: {}", accountId, fraudType);

        // Logic to notify security team
        // This might involve sending an email, creating a ticket, etc.
    }

    private void notifyUserOfFraudAlert(String accountId, String fraudType) {
        logger.info("Notifying user about fraud alert: {}", fraudType);

        messageProducer.sendNotificationEvent(
                accountId,
                "SECURITY ALERT",
                "We've detected suspicious activity on your account. Please contact our security team immediately.",
                "FRAUD_ALERT",
                KafkaMessage.Priority.CRITICAL
        );
    }

}

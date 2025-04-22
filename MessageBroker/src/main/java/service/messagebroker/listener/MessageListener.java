package service.messagebroker.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${spring.kafka.topics.analytics}")
    private String analyticsTopic;
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
            logger.error("Error processing transaction message: {}", message.getMessageId(), e);
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
            logger.error("Error processing notification message: {}", message.getMessageId(), e);
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
            logger.error("Error processing user activity message: {}", message.getMessageId(), e);
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
            logger.error("Error processing fraud alert message: {}", message.getMessageId(), e);
            acknowledgment.acknowledge();
        }
    }
    /**
     * Listen for transfer debited messages
     */
    @KafkaListener(
            topics = "${spring.kafka.topics.transfer-debited}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenTransferDebited(
            @Payload KafkaMessage message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment
    ) {
        try {
            logger.info("Received transfer debited message: {}", message.getMessageId());

            // Process the transfer debited message
            processTransferDebitedMessage(message);

            // Acknowledge the message
            acknowledgment.acknowledge();

            logger.info("Transfer debited message processed successfully: {}", message.getMessageId());
        } catch (Exception e) {
            logger.error("Error processing transfer debited message: {}", message.getMessageId(), e);
            acknowledgment.acknowledge();
        }
    }

    /**
     * Listen for transaction logged messages
     */
    @KafkaListener(
            topics = "${spring.kafka.topics.transaction-logged}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenTransactionLogged(
            @Payload KafkaMessage message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment
    ) {
        try {
            logger.info("Received transaction logged message: {}", message.getMessageId());

            // Process the transaction logged message
            processTransactionLoggedMessage(message);

            // Acknowledge the message
            acknowledgment.acknowledge();

            logger.info("Transaction logged message processed successfully: {}", message.getMessageId());
        } catch (Exception e) {
            logger.error("Error processing transaction logged message: {}", message.getMessageId(), e);
            acknowledgment.acknowledge();
        }
    }

    /**
     * Listen for transfer failed messages
     */
    @KafkaListener(
            topics = "${spring.kafka.topics.transfer-failed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenTransferFailed(
            @Payload KafkaMessage message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment
    ) {
        try {
            logger.info("Received transfer failed message: {}", message.getMessageId());

            // Process the transfer failed message
            handleFailedTransaction(message.getPayload());

            // Acknowledge the message
            acknowledgment.acknowledge();

            logger.info("Transfer failed message processed successfully: {}", message.getMessageId());
        } catch (Exception e) {
            logger.error("Error processing transfer failed message: {}", message.getMessageId(), e);
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

        // Send analytics data for all transaction types
        sendTransactionAnalytics(message);
    }
    /**
     * Send transaction data to analytics topic
     */
    private void sendTransactionAnalytics(KafkaMessage message) {
        String transactionId = (String) message.getPayload().getOrDefault("transactionId", "Unknown");
        logger.info("Sending transaction data to analytics for: {}", transactionId);

        // Clone the payload and add analytics metadata
        Map<String, Object> analyticsData = new HashMap<>(message.getPayload());
        analyticsData.put("processedTimestamp", System.currentTimeMillis());
        analyticsData.put("analyticsType", "TRANSACTION_TRACKING");
        analyticsData.put("action", message.getAction());

        // Create and send analytics message
        KafkaMessage analyticsMessage = new KafkaMessage(
                KafkaMessage.MessageType.ANALYTICS,
                "message-broker",
                message.getSubject(),
                "TRANSACTION_ANALYTICS"
        );
        analyticsMessage.setPayload(analyticsData);

        messageProducer.sendMessage(
                analyticsTopic,
                message.getSubject(),
                analyticsMessage
        );
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

    /**
     * Process transaction logged messages
     */
    private void processTransactionLoggedMessage(KafkaMessage message) {
        Map<String, Object> payload = message.getPayload();
        String transactionId = (String) payload.getOrDefault("transactionId", "Unknown");
        String type = (String) payload.getOrDefault("type", "Unknown");

        logger.info("Processing logged transaction: transactionId={}, type={}",
                transactionId, type);

        // Archive transaction data for audit purposes
        archiveTransactionData(payload);

        // Update analytics data
        updateTransactionAnalytics(payload);

        // If this is a significant transaction, we might want to notify relevant parties
        if (isSignificantTransaction(payload)) {
            notifyAboutSignificantTransaction(payload);
        }
    }
    /**
     * Archive transaction data for audit and compliance purposes
     */
    private void archiveTransactionData(Map<String, Object> payload) {
        String transactionId = (String) payload.getOrDefault("transactionId", "Unknown");
        logger.info("Archiving transaction data for audit: {}", transactionId);

        // Implementation would depend on the archiving system
        // This might involve storing in a database, sending to a data lake, etc.
    }

    /**
     * Update analytics with transaction data
     */
    private void updateTransactionAnalytics(Map<String, Object> payload) {
        // Extract relevant data for analytics
        String transactionId = (String) payload.getOrDefault("transactionId", "Unknown");
        String accountId = (String) payload.getOrDefault("accountId", "Unknown");
        String type = (String) payload.getOrDefault("type", "Unknown");

        logger.info("Updating analytics for transaction: {}", transactionId);

        // Create analytics message
        Map<String, Object> analyticsData = new HashMap<>(payload);
        analyticsData.put("processedTimestamp", System.currentTimeMillis());
        analyticsData.put("analyticsType", "TRANSACTION_ANALYSIS");

        // Create the KafkaMessage object
        KafkaMessage analyticsMessage = new KafkaMessage(
                KafkaMessage.MessageType.ANALYTICS,
                "message-broker",
                accountId,
                "TRANSACTION_ANALYTICS"
        );

// Set the payload
        analyticsMessage.setPayload(analyticsData);

// Send the message
        messageProducer.sendMessage(
                analyticsTopic,
                accountId,
                analyticsMessage
        );

    }

    /**
     * Determine if a transaction is significant based on criteria
     */
    private boolean isSignificantTransaction(Map<String, Object> payload) {
        // Check if transaction meets criteria for significance
        // This could be based on amount, type, frequency, etc.

        if (payload.containsKey("amount")) {
            try {
                double amount = Double.parseDouble(payload.get("amount").toString());
                // Example: Transactions over $10,000 are significant
                if (amount > 10000.0) {
                    return true;
                }
            } catch (NumberFormatException e) {
                logger.error("Invalid amount format in transaction", e);
            }
        }

        // Check for other significance criteria
        String type = (String) payload.getOrDefault("type", "");
        return "INTERNATIONAL".equals(type) || "HIGH_VALUE".equals(type);
    }

    /**
     * Notify relevant parties about significant transactions
     */
    private void notifyAboutSignificantTransaction(Map<String, Object> payload) {
        String transactionId = (String) payload.getOrDefault("transactionId", "Unknown");
        String accountId = (String) payload.getOrDefault("accountId", "Unknown");

        logger.info("Sending notification for significant transaction: {}", transactionId);

        // Notify compliance team
        // This would typically involve sending an email or creating a ticket

        // Optionally notify the account owner
        messageProducer.sendNotificationEvent(
                accountId,
                "Significant Transaction",
                "A significant transaction has been processed on your account. Please review for accuracy.",
                "SIGNIFICANT_TRANSACTION",
                KafkaMessage.Priority.HIGH
        );
    }

    /**
     * Process transfer debited messages
     */
    private void processTransferDebitedMessage(KafkaMessage message) {
        Map<String, Object> payload = message.getPayload();
        String accountId = message.getSubject();

        // Extract transfer details
        String transferId = (String) payload.getOrDefault("transferId", "Unknown");
        double amount = 0.0;
        try {
            if (payload.containsKey("amount")) {
                Object amountObj = payload.get("amount");
                if (amountObj instanceof Number) {
                    amount = ((Number) amountObj).doubleValue();
                } else {
                    amount = Double.parseDouble(amountObj.toString());
                }
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid amount format in transfer debited message", e);
            amount = 0.0;
        }
        String recipientId = (String) payload.getOrDefault("recipientId", "Unknown");

        logger.info("Processing transfer debited: accountId={}, transferId={}, amount={}, recipient={}",
                accountId, transferId, amount, recipientId);

        // Record the debit transaction
        recordDebitTransaction(accountId, transferId, amount);

        // Check for unusual transfer patterns
        if (isUnusualTransfer(accountId, amount, recipientId)) {
            reportUnusualTransfer(accountId, transferId, amount, recipientId);
        }

        // Send notification to the user about the debit
        notifyUserOfDebit(accountId, transferId, amount, recipientId);

        // Update account activity records
        updateAccountActivity(accountId, payload);
    }

    /**
     * Record the debit transaction in the system
     */
    private void recordDebitTransaction(String accountId, String transferId, double amount) {
        logger.info("Recording debit transaction: account={}, transfer={}, amount={}",
                accountId, transferId, amount);

        // Implementation would depend on the transaction recording system
        // This might involve updating a database, sending to a transaction service, etc.

        // For now, we'll just log the transaction details
        Map<String, Object> transactionRecord = new HashMap<>();
        transactionRecord.put("accountId", accountId);
        transactionRecord.put("transferId", transferId);
        transactionRecord.put("amount", amount);
        transactionRecord.put("timestamp", System.currentTimeMillis());
        transactionRecord.put("type", "DEBIT");

        // In a real implementation, this would save to a database or call another service
        logger.debug("Transaction record created: {}", transactionRecord);
    }

    /**
     * Check if this is an unusual transfer based on patterns
     */
    private boolean isUnusualTransfer(String accountId, double amount, String recipientId) {
        // Check for unusual transfer patterns
        // This could be based on amount, frequency, recipient, etc.

        // Example: Transfers over $5,000 might be considered unusual
        if (amount > 5000.0) {
            logger.info("Detected high-value transfer: account={}, amount={}", accountId, amount);
            return true;
        }

        // More sophisticated checks would be implemented here
        // For example, checking if this is a new recipient, or if there have been many transfers recently

        return false;
    }


    /**
     * Report an unusual transfer for further review
     */
    private void reportUnusualTransfer(String accountId, String transferId, double amount, String recipientId) {
        logger.warn("Reporting unusual transfer for review: account={}, transfer={}, amount={}",
                accountId, transferId, amount);

        // Create alert for review
        Map<String, Object> alertDetails = new HashMap<>();
        alertDetails.put("accountId", accountId);
        alertDetails.put("transferId", transferId);
        alertDetails.put("amount", amount);
        alertDetails.put("recipientId", recipientId);
        alertDetails.put("timestamp", System.currentTimeMillis());
        alertDetails.put("reason", "Unusual transfer amount");

        // Send to fraud alert topic for review
        messageProducer.sendFraudAlertEvent(
                accountId,
                transferId,
                "UNUSUAL_TRANSFER",
                alertDetails
        );
    }

    /**
     * Notify the user about the debit from their account
     */
    private void notifyUserOfDebit(String accountId, String transferId, double amount, String recipientId) {
        logger.info("Sending debit notification to user: {}", accountId);

        // Create a user-friendly message
        String message = String.format("Your account has been debited $%.2f for transfer %s",
                amount, transferId);

        // Add recipient information if available
        if (!"Unknown".equals(recipientId)) {
            message += String.format(" to recipient %s", recipientId);
        }

        // Send notification to the user
        messageProducer.sendNotificationEvent(
                accountId,
                "Transfer Completed",
                message,
                "TRANSFER_DEBIT",
                KafkaMessage.Priority.MEDIUM
        );
    }

    /**
     * Update account activity records
     */
    private void updateAccountActivity(String accountId, Map<String, Object> details) {
        logger.info("Updating account activity: account={}, type={}", accountId, "TRANSFER_DEBIT");

        // Create activity record
        Map<String, Object> activityData = new HashMap<>(details);
        activityData.put("activityType", "TRANSFER_DEBIT");
        activityData.put("timestamp", System.currentTimeMillis());

        // Send to user activity topic
        messageProducer.sendUserActivityEvent(
                accountId,
                "ACCOUNT_" + System.currentTimeMillis(),
                "USER_ACTIVITY",
                activityData
        );
    }
}

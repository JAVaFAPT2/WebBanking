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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@Component
public class MessageProducer {
    private static final Logger logger = LoggerFactory.getLogger(MessageProducer.class);

    private final KafkaTemplate<String, KafkaMessage> kafkaTemplate;

    @Autowired
    public MessageProducer(KafkaTemplate<String, KafkaMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

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

    public CompletableFuture<SendResult<String, KafkaMessage>> sendTransactionEvent(
            UUID accountId, String transactionId, double amount, String description, boolean isInternational) {

        String accountIdStr = accountId.toString();
        KafkaMessage message = new KafkaMessage(
                KafkaMessage.MessageType.TRANSACTION,
                "transaction-service",
                accountIdStr,
                "TRANSACTION_CREATED"
        );
        message.setPriority(amount > 10000 ? KafkaMessage.Priority.HIGH : KafkaMessage.Priority.MEDIUM);

        Map<String, Object> payload = new HashMap<>();
        payload.put("transactionId", transactionId);
        payload.put("accountId", accountIdStr);
        payload.put("amount", amount);
        payload.put("description", description);
        payload.put("timestamp", java.time.LocalDateTime.now().toString());
        payload.put("isInternational", isInternational);

        message.setPayload(payload);

        return sendMessage("transaction-topic", accountIdStr, message);
    }

    public CompletableFuture<SendResult<String, KafkaMessage>> sendNotificationEvent(
            UUID userId, String title, String content, String notificationType, KafkaMessage.Priority priority) {

        String userIdStr = userId.toString();
        KafkaMessage message = new KafkaMessage(
                KafkaMessage.MessageType.NOTIFICATION,
                "notification-service",
                userIdStr,
                "NOTIFICATION_CREATED"
        );
        message.setPriority(priority);

        Map<String, Object> payload = new HashMap<>();
        payload.put("title", title);
        payload.put("content", content);
        payload.put("timestamp", java.time.LocalDateTime.now().toString());
        payload.put("notificationType", notificationType);
        payload.put("read", false);

        message.setPayload(payload);

        return sendMessage("notification-topic", userIdStr, message);
    }

    public CompletableFuture<SendResult<String, KafkaMessage>> sendUserActivityEvent(
            UUID userId, String activity, String userActivity, Map<String, Object> details) {

        String userIdStr = userId.toString();
        KafkaMessage message = new KafkaMessage(
                KafkaMessage.MessageType.USER_ACTIVITY,
                "user-service",
                userIdStr,
                userActivity
        );
        message.setPriority(KafkaMessage.Priority.LOW);

        Map<String, Object> payload = new HashMap<>(details);
        payload.put("timestamp", java.time.LocalDateTime.now().toString());
        payload.put("userId", userIdStr);
        payload.put("ipAddress", details.getOrDefault("ipAddress", "unknown"));
        payload.put("userAgent", details.getOrDefault("userAgent", "unknown"));
        payload.put("activity", activity);

        message.setPayload(payload);

        return sendMessage("user-activity-topic", userIdStr, message);
    }

    public CompletableFuture<SendResult<String, KafkaMessage>> sendFraudAlertEvent(
            String accountId, String transactionId, String fraudType, Map<String, Object> details) {

        KafkaMessage message = new KafkaMessage(
                KafkaMessage.MessageType.SYSTEM_ALERT,
                "fraud-detection-service",
                accountId,
                "FRAUD_DETECTED"
        );
        message.setPriority(KafkaMessage.Priority.CRITICAL);

        Map<String, Object> payload = new HashMap<>(details);
        payload.put("transactionId", transactionId);
        payload.put("accountId", accountId);
        payload.put("fraudType", fraudType);
        payload.put("detectionTime", java.time.LocalDateTime.now().toString());
        payload.put("severity", "HIGH");

        message.setPayload(payload);

        return sendMessage("fraud-alert-topic", accountId, message);
    }

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
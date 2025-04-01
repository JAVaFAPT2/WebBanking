package service.loggingservice.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Publisher for log events.
 */
@Service
public class LogEventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogEventPublisher.class);
    private final KafkaTemplate<String, String> kafkaTemplate;

    public LogEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishLogEvent(String topic, String message) {
        LOGGER.info("Publishing log event to Kafka - Topic: {}, Message: {}", topic, message);
        kafkaTemplate.send(topic, message);
    }

    public void logNotificationCreated(String notificationId) {
        String message = String.format("Notification created with ID: %s", notificationId);
        publishLogEvent("notification-logs", message);
    }

    public void logNotificationUpdated(String notificationId) {
        String message = String.format("Notification updated with ID: %s", notificationId);
        publishLogEvent("notification-logs", message);
    }

    public void logNotificationDeleted(String notificationId) {
        String message = String.format("Notification deleted with ID: %s", notificationId);
        publishLogEvent("notification-logs", message);
    }
}
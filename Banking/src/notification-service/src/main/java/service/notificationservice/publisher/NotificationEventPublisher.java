package service.notificationservice.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import service.notificationservice.entity.NotificationEntity;

@Service
public class NotificationEventPublisher {


    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationEventPublisher.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public NotificationEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public void publishEvent(String topic, String message) {
        LOGGER.info("Publishing event to Kafka - Topic: {}, Message: {}", topic, message);
        kafkaTemplate.send(topic, message);
    }

    public void sendMessageToBroker(NotificationEntity notification) {
        try {
            String message = objectMapper.writeValueAsString(notification);
            LOGGER.info("Publishing event to Kafka - Topic: notification-topic, Message: {}", message);
            kafkaTemplate.send("notification-topic", message);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error serializing notification to JSON", e);
        }
    }
}

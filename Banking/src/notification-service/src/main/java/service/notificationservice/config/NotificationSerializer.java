package service.notificationservice.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;
import service.notificationservice.entity.NotificationEntity;

import java.util.Map;

public class NotificationSerializer implements Serializer<NotificationEntity> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // Configuration logic if needed
    }

    @Override
    public byte[] serialize(String topic, NotificationEntity notification) {
        try {
            return objectMapper.writeValueAsBytes(notification);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing Notification", e);
        }
    }

    @Override
    public void close() {
        // Cleanup logic if needed
    }
}

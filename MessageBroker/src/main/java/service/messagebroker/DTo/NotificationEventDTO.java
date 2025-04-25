package service.messagebroker.DTo;

import service.messagebroker.models.KafkaMessage;

import java.util.UUID;

public record NotificationEventDTO(
        UUID UserId,
        String Title,
        String Content,
        String Notification,
        KafkaMessage.Priority priority
) {
}

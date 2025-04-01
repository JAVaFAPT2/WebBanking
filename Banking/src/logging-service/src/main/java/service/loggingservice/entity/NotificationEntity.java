package service.loggingservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import service.loggingservice.event.LogEventListener;

@Entity
@EntityListeners(LogEventListener.class)
public class NotificationEntity extends service.notificationservice.entity.NotificationEntity {
}

package service.notificationservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import service.notificationservice.listener.NotificationEventListener;
import service.shared.models.Notification;
@Entity
@EntityListeners(NotificationEventListener.class)
public class NotificationEntity extends Notification {

}

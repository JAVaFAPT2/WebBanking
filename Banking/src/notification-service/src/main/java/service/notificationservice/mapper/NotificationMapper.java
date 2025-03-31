package service.notificationservice.mapper;

import service.notificationservice.entity.NotificationEntity;
import service.shared.models.Notification;

public class NotificationMapper {
    public static Notification toNotification(NotificationEntity entity) {
        Notification notification = new Notification();
        notification.setId(entity.getId());
        notification.setMessage(entity.getMessage());
        // Map other fields as necessary
        return notification;
    }

    public static NotificationEntity toNotificationEntity(Notification notification) {
        NotificationEntity entity = new NotificationEntity();
        entity.setId(notification.getId());
        entity.setMessage(notification.getMessage());
        // Map other fields as necessary
        return entity;
    }

}

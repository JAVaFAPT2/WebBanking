package service.notificationservice.service;

import service.notificationservice.entity.NotificationEntity;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    NotificationEntity createNotification(NotificationEntity notification);
    List<NotificationEntity> getAllNotifications();
    void deleteNotification(UUID id);
    NotificationEntity getNotificationById(UUID id);

    NotificationEntity updateNotification(NotificationEntity notification);
}

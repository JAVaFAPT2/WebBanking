package service.notificationservice.service.iml;

import org.springframework.stereotype.Service;
import service.notificationservice.entity.NotificationEntity;
import service.notificationservice.publisher.NotificationEventPublisher;
import service.notificationservice.repository.NotificationRepository;
import service.notificationservice.service.NotificationService;

import java.util.List;
import java.util.UUID;
@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationEventPublisher notificationEventPublisher;

    public NotificationServiceImpl(NotificationRepository notificationRepository, NotificationEventPublisher notificationEventPublisher) {
        this.notificationRepository = notificationRepository;
        this.notificationEventPublisher = notificationEventPublisher;
    }

    @Override
    public NotificationEntity createNotification(NotificationEntity notification) {
        NotificationEntity savedNotification = notificationRepository.save(notification);
        notificationEventPublisher.publishEvent("notification-topic", "Notification created: " + savedNotification.getId());
        return savedNotification;
    }

    @Override
    public List<NotificationEntity> getAllNotifications() {
        notificationEventPublisher.publishEvent("notification-topic", "Retrieving all notifications");
        return notificationRepository.findAll();

    }

    @Override
    public void deleteNotification(UUID id) {
        notificationRepository.deleteById(id);
        notificationEventPublisher.publishEvent("notification-topic", "Notification deleted: " + id);

    }


    @Override
    public NotificationEntity getNotificationById(UUID id) {
        notificationEventPublisher.publishEvent("notification-topic", "Retrieved notification: " + id);
        return notificationRepository.findById(id).orElse(null);

    }
    @Override
    public NotificationEntity updateNotification(NotificationEntity notification) {
        NotificationEntity updatedNotification = notificationRepository.save(notification);
        notificationEventPublisher.publishEvent("notification-topic", "Notification updated: " + updatedNotification.getId());
        return updatedNotification;
    }

}

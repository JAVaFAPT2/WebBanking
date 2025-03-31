package service.notificationservice.listener;


import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.notificationservice.entity.NotificationEntity;


import java.time.LocalDateTime;


/**
 * Event listener for Notification entity events.
 */
public class NotificationEventListener {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEventListener.class);

    @PrePersist
    public void prePersist(NotificationEntity notification) {
        // Automatically set the timestamp if not set
        if (notification.getTimestamp() == null) {
            notification.setTimestamp(LocalDateTime.now());
        }

        // Log the creation of the notification
        logger.info("Notification created with ID: {}", notification.getId());
    }

    @PostPersist
    public void postPersist(NotificationEntity notification) {
        // Logic after the notification is persisted
        logger.info("Notification persisted with ID: {}", notification.getId());
         sendMessageToBroker(notification);
    }

    @PreUpdate
    public void preUpdate(NotificationEntity notification) {
        // Logic before the notification is updated
        logger.info("Notification with ID {} is about to be updated", notification.getId());
        notification.setTimestamp(LocalDateTime.now());
    }

    @PostUpdate
    public void postUpdate(NotificationEntity notification) {
        // Logic after the notification is updated
        logger.info("Notification with ID {} has been updated", notification.getId());


        triggerExternalService(notification);
    }

    @PreRemove
    public void preRemove(NotificationEntity notification) {
        // Logic before the notification is removed
        logger.info("Notification with ID {} is about to be removed", notification.getId());
        archiveNotification(notification);
    }

    @PostRemove
    public void postRemove(NotificationEntity notification) {
        // Logic after the notification is removed
        logger.info("Notification with ID {} has been removed", notification.getId());
    }

    // Example method to send a message to a message broker
    private void sendMessageToBroker(NotificationEntity notification) {
        // Implementation for sending a message to a message broker
        logger.info("Sending notification to message broker: {}", notification.getMessage());
    }

    // Example method to trigger an external service
    private void triggerExternalService(NotificationEntity notification) {
        logger.info("Triggering external service for notification: {}", notification.getId());
    }

    // Example method to archive a notification
    private void archiveNotification(NotificationEntity notification) {

        logger.info("Archiving notification: {}", notification.getId());
    }
}
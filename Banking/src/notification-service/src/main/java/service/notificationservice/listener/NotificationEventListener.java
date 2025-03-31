package service.notificationservice.listener;


import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.shared.models.Notification;

import java.time.LocalDateTime;


/**
 * Event listener for Notification entity events.
 */
public class NotificationEventListener {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEventListener.class);

    @PrePersist
    public void prePersist(Notification notification) {
        // Automatically set the timestamp if not set
        if (notification.getTimestamp() == null) {
            notification.setTimestamp(LocalDateTime.now());
        }

        // Log the creation of the notification
        logger.info("Notification created with ID: {}", notification.getId());
    }

    @PostPersist
    public void postPersist(Notification notification) {
        // Logic after the notification is persisted
        logger.info("Notification persisted with ID: {}", notification.getId());

        // Example: Send a message to a message broker (e.g., Kafka)
        // sendMessageToBroker(notification);
    }

    @PreUpdate
    public void preUpdate(Notification notification) {
        // Logic before the notification is updated
        logger.info("Notification with ID {} is about to be updated", notification.getId());

        // Example: Update the timestamp when the notification is updated
        notification.setTimestamp(LocalDateTime.now());
    }

    @PostUpdate
    public void postUpdate(Notification notification) {
        // Logic after the notification is updated
        logger.info("Notification with ID {} has been updated", notification.getId());

        // Example: Trigger an external service call
        // triggerExternalService(notification);
    }

    @PreRemove
    public void preRemove(Notification notification) {
        // Logic before the notification is removed
        logger.info("Notification with ID {} is about to be removed", notification.getId());

        // Example: Archive the notification before deletion
        // archiveNotification(notification);
    }

    @PostRemove
    public void postRemove(Notification notification) {
        // Logic after the notification is removed
        logger.info("Notification with ID {} has been removed", notification.getId());
    }

    // Example method to send a message to a message broker
    private void sendMessageToBroker(Notification notification) {
        // Implementation for sending a message to a message broker
        logger.info("Sending notification to message broker: {}", notification.getMessage());
    }

    // Example method to trigger an external service
    private void triggerExternalService(Notification notification) {
        // Implementation for triggering an external service
        logger.info("Triggering external service for notification: {}", notification.getId());
    }

    // Example method to archive a notification
    private void archiveNotification(Notification notification) {
        // Implementation for archiving a notification
        logger.info("Archiving notification: {}", notification.getId());
    }
}
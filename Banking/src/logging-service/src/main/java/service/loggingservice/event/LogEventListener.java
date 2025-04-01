package service.loggingservice.event;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import service.notificationservice.entity.NotificationEntity;

@Component
public class LogEventListener {

    private static final Logger logger = LoggerFactory.getLogger(LogEventListener.class);
    private final LogEventPublisher logEventPublisher;

    public LogEventListener(LogEventPublisher logEventPublisher) {
        this.logEventPublisher = logEventPublisher;
    }

    @PostPersist
    public void postPersist(NotificationEntity notification) {
        logger.info("Notification created with ID: {}", notification.getId());
        logEventPublisher.logNotificationCreated(notification.getId().toString());
    }

    @PostUpdate
    public void postUpdate(NotificationEntity notification) {
        logger.info("Notification updated with ID: {}", notification.getId());
        logEventPublisher.logNotificationUpdated(notification.getId().toString());
    }

    @PostRemove
    public void postRemove(NotificationEntity notification) {
        logger.info("Notification deleted with ID: {}", notification.getId());
        logEventPublisher.logNotificationDeleted(notification.getId().toString());
    }
}

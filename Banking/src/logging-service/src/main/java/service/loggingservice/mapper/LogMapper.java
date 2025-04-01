package service.loggingservice.mapper;

import service.loggingservice.entity.LogEntity;
import service.shared.models.Log;
import service.shared.models.Notification;

public class LogMapper {
    public static Log toLog(LogEntity entity) {
        Log log = new Log();
        log.setId(entity.getId());
        log.setMessage(entity.getMessage());
        log.setLevel(entity.getLevel());
        log.setTimestamp(entity.getTimestamp());
        log.setServiceName(entity.getServiceName());
        return log;
    }

    public static LogEntity toNotificationEntity(Log notification) {
        LogEntity entity = new LogEntity();
        entity.setId(notification.getId());
        entity.setMessage(notification.getMessage());
        entity.setLevel(notification.getLevel());
        entity.setTimestamp(notification.getTimestamp());
        entity.setServiceName(notification.getServiceName());
        return entity;
    }
}

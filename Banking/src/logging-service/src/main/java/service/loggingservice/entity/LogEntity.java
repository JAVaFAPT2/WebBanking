package service.loggingservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import service.loggingservice.event.LogEventListener;
import service.shared.models.Log;

@Entity
@EntityListeners(LogEventListener.class)
public class LogEntity extends Log {
}

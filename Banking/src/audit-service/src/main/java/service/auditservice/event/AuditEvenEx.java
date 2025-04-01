package service.auditservice.event;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import lombok.Setter;
import service.auditservice.event.listener.AuditEventListener;
import service.shared.event.AuditEvent;

import java.time.LocalDateTime;




public class AuditEvenEx extends AuditEvent {
    public AuditEvenEx(Object source, String message, String details) {
        super(source, message, details);
        this.eventTimestamp = LocalDateTime.now();
    }
}

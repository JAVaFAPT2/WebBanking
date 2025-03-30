package service.shared.listener;

import jakarta.persistence.PrePersist;
import service.shared.event.AuditEvent;


import java.time.LocalDateTime;
import java.util.UUID;

public class AuditEventListener {

    @PrePersist
    public void prePersist(AuditEvent auditEvent) {
        // Automatically set the timestamp if not set
        if (auditEvent.getTimestamp() == null) {
            auditEvent.setTimestamp(LocalDateTime.now());
        }
        // If using UUID and not using a generation strategy, generate one
        if (auditEvent.getId() == null) {
            auditEvent.setId(UUID.randomUUID());
        }
    }
}

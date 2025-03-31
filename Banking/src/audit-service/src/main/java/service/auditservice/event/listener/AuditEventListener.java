package service.auditservice.event.listener;

import jakarta.persistence.PrePersist;
import service.shared.event.AuditEvent;


import java.time.LocalDateTime;
import java.util.UUID;

public class AuditEventListener {

    @PrePersist
    public void prePersist(AuditEvent auditEvent) {
        if (auditEvent.getTimestamp() == null) {
            auditEvent.setTimestamp(LocalDateTime.now());
        }
        if (auditEvent.getId() == null) {
            auditEvent.setId(UUID.randomUUID());
        }
    }
}

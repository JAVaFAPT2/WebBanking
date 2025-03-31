package service.auditservice.event;

import jakarta.persistence.EntityListeners;
import service.auditservice.event.listener.AuditEventListener;
import service.shared.event.AuditEvent;
@EntityListeners(AuditEventListener.class)
public class AuditEvenEx extends AuditEvent {
}

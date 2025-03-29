package service.auditservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.auditservice.repository.AuditRepository;
import service.shared.event.AuditEvent;


import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for audit event handling.
 */
@Service
public class AuditService {

    @Autowired
    private AuditRepository auditRepository;

    /**
     * Logs a new audit event.
     *
     * @param auditEvent The audit event to log.
     * @return The saved audit event.
     */
    public AuditEvent logEvent(AuditEvent auditEvent) {
        // Set a timestamp if not already provided
        if (auditEvent.getTimestamp() == null) {
            auditEvent.setTimestamp(LocalDateTime.now());
        }
        return auditRepository.save(auditEvent);
    }

    /**
     * Retrieves all audit events.
     *
     * @return A list of audit events.
     */
    public List<AuditEvent> getAllEvents() {
        return auditRepository.findAll();
    }
}

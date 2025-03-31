package service.auditservice.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.auditservice.event.AuditEvenEx;
import service.auditservice.repository.AuditRepository;
import service.auditservice.service.AuditService;

import java.time.LocalDateTime;
import java.util.List;
@Service
public class AuditServiceImpl implements AuditService {
    @Autowired
    private AuditRepository auditRepository;

    /**
     * Logs a new audit event.
     *
     * @param auditEvent The audit event to log.
     * @return The saved audit event.
     */
    @Override
    public AuditEvenEx logEvent(AuditEvenEx auditEvent) {
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
    @Override
    public List<AuditEvenEx> getAllEvents() {
        return auditRepository.findAll();
    }
}

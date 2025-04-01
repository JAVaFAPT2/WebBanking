package service.auditservice.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import service.auditservice.event.AuditEvenEx;
import service.auditservice.model.AuditLogEntity;
import service.auditservice.repository.AuditLogRepository;

@Component
public class AuditEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditEventListener.class);
    private final AuditLogRepository auditLogRepository;

    public AuditEventListener(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }


    @EventListener
    public void handleAuditEvent(AuditEvenEx event) {
        LOGGER.info("Received audit event: {}", event);
        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.setMessage(event.getMessage());
        auditLog.setTimestamp(event.getEventTimestamp());
        auditLog.setDetails(String.valueOf(event.getDetails()));
        auditLogRepository.save(auditLog);
    }
}
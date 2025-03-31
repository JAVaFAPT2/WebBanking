package service.auditservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.auditservice.event.AuditEvenEx;
import service.auditservice.repository.AuditRepository;



import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for audit event handling.
 */

public interface AuditService {
    AuditEvenEx logEvent(AuditEvenEx auditEvent);
    List<AuditEvenEx> getAllEvents();

}

package service.auditservice.service;

import service.auditservice.model.AuditLogEntity;
import service.shared.models.AuditLog;

import java.util.List;
import java.util.UUID;

public interface AuditLogService {
    AuditLogEntity save(AuditLogEntity auditLog);
    List<AuditLogEntity> getAllAuditLogs();
    AuditLogEntity getAuditLogById(UUID id);


}

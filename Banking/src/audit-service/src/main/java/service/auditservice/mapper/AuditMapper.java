package service.auditservice.mapper;

import service.auditservice.model.AuditLogEntity;
import service.shared.event.AuditEvent;
import service.shared.models.AuditLog;

public class AuditMapper {
    public static AuditLog toAuditLog(AuditLogEntity entity) {
        if (entity == null) {
            return null;
        }
        return AuditLog.builder()
                .id(entity.getId())
                .message(entity.getMessage())
                .details(entity.getDetails())
                .timestamp(entity.getTimestamp())
                .build();
    }

    // Convert AuditLog to AuditLogEntity
    public static AuditLogEntity toAuditLogEntity(AuditLog auditLog) {
        if (auditLog == null) {
            return null;
        }
        AuditLogEntity entity = new AuditLogEntity();
        entity.setId(auditLog.getId());
        entity.setMessage(auditLog.getMessage());
        entity.setDetails(auditLog.getDetails());
        entity.setTimestamp(auditLog.getTimestamp());
        return entity;
    }
}

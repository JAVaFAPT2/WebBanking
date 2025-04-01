package service.auditservice.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import service.shared.models.AuditLog;

import java.time.LocalDateTime;


@Getter
@Setter
public class AuditLogEntity extends AuditLog {
}

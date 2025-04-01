package service.auditservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import service.auditservice.model.AuditLogEntity;
import service.auditservice.service.AuditLogService;
import service.shared.models.ApiResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {
    private final AuditLogService auditLogService;
    @Autowired
    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }
    @PostMapping
    public ApiResponse<AuditLogEntity> createAuditLog(@RequestBody AuditLogEntity auditLogEntity)
    {
        AuditLogEntity createdAuditLog  = auditLogService.save(auditLogEntity);
        return new ApiResponse<>(true, createdAuditLog , "Audit log created successfully", HttpStatus.CREATED);
    }
    @GetMapping
    public ApiResponse<List<AuditLogEntity>> getAllAuditLogs() {
        List<AuditLogEntity> auditLogs = auditLogService.getAllAuditLogs();
        return new ApiResponse<>(true, auditLogs, "Audit logs retrieved successfully", HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ApiResponse<AuditLogEntity> getAuditLogById(@PathVariable UUID id) {
        AuditLogEntity auditLog = auditLogService.getAuditLogById(id);
        if (auditLog != null) {
            return new ApiResponse<>(true,auditLog,"Audit log retrieved successfully", HttpStatus.OK);
        } else {
            return new ApiResponse<>(true, null, "Audit log not found", HttpStatus.NOT_FOUND);
        }
    }
}

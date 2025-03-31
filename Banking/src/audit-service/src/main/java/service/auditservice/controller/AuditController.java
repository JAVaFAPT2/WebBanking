package service.auditservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import service.auditservice.event.AuditEvenEx;
import service.auditservice.service.Impl.AuditServiceImpl;
import service.shared.models.ApiResponse;

import java.util.List;

/**
 * REST controller for audit tracking.
 */
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    @Autowired
    private AuditServiceImpl auditService;

    /**
     * Endpoint to record an audit event.
     *
     * @param auditEvent The audit event details.
     * @return The saved audit event.
     */
    @PostMapping("/log")
    public ApiResponse<AuditEvenEx> logAuditEvent(@RequestBody AuditEvenEx auditEvent) {
    AuditEvenEx savedEvent = auditService.logEvent(auditEvent);
    return new ApiResponse<>(true, savedEvent, "Audit event logged successfully", HttpStatus.OK);

    }

    /**
     * Endpoint to retrieve all audit events.
     *
     * @return A list of audit events.
     */
    @GetMapping("/logs")
    public ApiResponse<List<AuditEvenEx>> getAuditLogs() {
        List<AuditEvenEx> logs = auditService.getAllEvents();
        return new ApiResponse<>(true, logs, "Audit logs retrieved successfully", HttpStatus.OK);
    }
}

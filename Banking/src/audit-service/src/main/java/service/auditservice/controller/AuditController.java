package service.auditservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.auditservice.service.AuditService;
import service.shared.event.AuditEvent;


import java.util.List;

/**
 * REST controller for audit tracking.
 */
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    @Autowired
    private AuditService auditService;

    /**
     * Endpoint to record an audit event.
     *
     * @param auditEvent The audit event details.
     * @return The saved audit event.
     */
    @PostMapping("/log")
    public ResponseEntity<AuditEvent> logAuditEvent(@RequestBody AuditEvent auditEvent) {
        AuditEvent savedEvent = auditService.logEvent(auditEvent);
        return ResponseEntity.ok(savedEvent);
    }

    /**
     * Endpoint to retrieve all audit events.
     *
     * @return A list of audit events.
     */
    @GetMapping("/logs")
    public ResponseEntity<List<AuditEvent>> getAuditLogs() {
        List<AuditEvent> logs = auditService.getAllEvents();
        return ResponseEntity.ok(logs);
    }
}

package service.monitorservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.monitorservice.model.AlertSeverity;
import service.monitorservice.service.AlertService;

/**
 * REST controller for alert management operations
 */
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    @Autowired
    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    /**
     * Send an alert with specified severity
     */
    @PostMapping
    public ResponseEntity<String> sendAlert(
            @RequestParam String serviceId,
            @RequestParam String message,
            @RequestParam(defaultValue = "INFO") String severity) {

        AlertSeverity alertSeverity;
        try {
            alertSeverity = AlertSeverity.valueOf(severity.toUpperCase());
        } catch (IllegalArgumentException e) {
            alertSeverity = AlertSeverity.INFO;
        }

        alertService.sendAlert(serviceId, message, alertSeverity);
        return ResponseEntity.ok("Alert sent successfully");
    }

    /**
     * Send an informational alert
     */
    @PostMapping("/info")
    public ResponseEntity<String> sendInfoAlert(
            @RequestParam String serviceId,
            @RequestParam String message) {

        alertService.sendInfoAlert(serviceId, message);
        return ResponseEntity.ok("Info alert sent successfully");
    }

    /**
     * Send a critical alert
     */
    @PostMapping("/critical")
    public ResponseEntity<String> sendCriticalAlert(
            @RequestParam String serviceId,
            @RequestParam String message) {

        alertService.sendCriticalAlert(serviceId, message);
        return ResponseEntity.ok("Critical alert sent successfully");
    }

    /**
     * Update an alert threshold
     */
    @PutMapping("/thresholds/{metricName}")
    public ResponseEntity<String> updateAlertThreshold(
            @PathVariable String metricName,
            @RequestParam Double threshold) {

        alertService.updateAlertThreshold(metricName, threshold);
        return ResponseEntity.ok("Alert threshold updated successfully");
    }

    /**
     * Check a metric against its threshold and trigger an alert if needed
     */
    @PostMapping("/check")
    public ResponseEntity<Boolean> checkAndAlert(
            @RequestParam String metricName,
            @RequestParam Double value,
            @RequestParam String serviceId) {

        boolean alertTriggered = alertService.checkAndAlert(metricName, value, serviceId);
        return ResponseEntity.ok(alertTriggered);
    }

    /**
     * Clear alert history
     */
    @DeleteMapping("/history")
    public ResponseEntity<String> clearAlertHistory() {
        alertService.clearAlertHistory();
        return ResponseEntity.ok("Alert history cleared successfully");
    }
}

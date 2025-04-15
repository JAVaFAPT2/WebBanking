package service.monitorservice.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.monitorservice.model.AlertSeverity;
import service.monitorservice.service.MonitoringService;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for monitoring operations
 */
@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private final MonitoringService monitoringService;

    // Store created counters and timers for reuse
    private final Map<String, Counter> counters = new HashMap<>();
    private final Map<String, Timer> timers = new HashMap<>();

    @Autowired
    public MonitoringController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    /**
     * Record a metric value
     */
    @PostMapping("/metrics")
    public ResponseEntity<String> recordMetric(
            @RequestParam String name,
            @RequestParam double value,
            @RequestParam(required = false) String[] tags) {

        monitoringService.recordMetric(name, value, tags != null ? tags : new String[0]);
        return ResponseEntity.ok("Metric recorded successfully");
    }

    /**
     * Create or get a counter and increment it
     */
    @PostMapping("/counters/{name}/increment")
    public ResponseEntity<String> incrementCounter(
            @PathVariable String name,
            @RequestParam(defaultValue = "1.0") double amount,
            @RequestParam(required = false) String[] tags) {

        Counter counter = counters.computeIfAbsent(name,
                k -> monitoringService.createCounter(name, tags != null ? tags : new String[0]));

        counter.increment(amount);
        return ResponseEntity.ok("Counter incremented successfully");
    }

    /**
     * Create or get a timer and record a timing
     */
    @PostMapping("/timers/{name}/record")
    public ResponseEntity<String> recordTiming(
            @PathVariable String name,
            @RequestParam long timeInMs,
            @RequestParam(required = false) String[] tags) {

        Timer timer = timers.computeIfAbsent(name,
                k -> monitoringService.createTimer(name, tags != null ? tags : new String[0]));

        timer.record(() -> {
            try {
                Thread.sleep(timeInMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        return ResponseEntity.ok("Timing recorded successfully");
    }

    /**
     * Trigger an alert
     */
    @PostMapping("/alerts")
    public ResponseEntity<String> triggerAlert(
            @RequestParam String name,
            @RequestParam String message,
            @RequestParam AlertSeverity severity) {

        monitoringService.triggerAlert(name, message, severity);
        return ResponseEntity.ok("Alert triggered successfully");
    }

    /**
     * Aggregate logs
     */
    @PostMapping("/logs")
    public ResponseEntity<String> aggregateLogs(
            @RequestParam String serviceId,
            @RequestParam String logContent) {

        monitoringService.aggregateLogs(serviceId, logContent);
        return ResponseEntity.ok("Logs aggregated successfully");
    }

    /**
     * Report service health status
     */
    @PostMapping("/health")
    public ResponseEntity<String> reportHealth(
            @RequestParam String serviceName,
            @RequestParam boolean isHealthy) {

        monitoringService.monitorHealthCheck(serviceName, isHealthy);
        return ResponseEntity.ok("Health status reported successfully");
    }
}

package service.monitorservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.monitorservice.service.LogAggregationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller for log-related operations
 */
@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogAggregationService logAggregationService;

    @Autowired
    public LogController(LogAggregationService logAggregationService) {
        this.logAggregationService = logAggregationService;
    }

    /**
     * Submit a log entry manually
     */
    @PostMapping("/submit")
    public ResponseEntity<String> submitLog(
            @RequestParam String serviceName,
            @RequestParam String level,
            @RequestParam String message) {

        logAggregationService.sendLog(serviceName, level, message);
        return ResponseEntity.ok("Log submitted successfully");
    }

    /**
     * Get recent logs for a service
     */
    @GetMapping("/service/{serviceName}")
    public ResponseEntity<List<LogAggregationService.LogEntry>> getServiceLogs(
            @PathVariable String serviceName,
            @RequestParam(defaultValue = "100") int limit) {

        return ResponseEntity.ok(logAggregationService.getRecentLogsForService(serviceName, limit));
    }

    /**
     * Get recent error logs
     */
    @GetMapping("/errors")
    public ResponseEntity<List<LogAggregationService.LogEntry>> getErrorLogs(
            @RequestParam(defaultValue = "100") int limit) {

        return ResponseEntity.ok(logAggregationService.getRecentErrorLogs(limit));
    }

    /**
     * Search logs with various filters
     */
    @GetMapping("/search")
    public ResponseEntity<List<LogAggregationService.LogEntry>> searchLogs(
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime) {

        return ResponseEntity.ok(logAggregationService.searchLogs(
                serviceName, level, searchText, startTime, endTime));
    }

    /**
     * Get log count by service
     */
    @GetMapping("/stats/by-service")
    public ResponseEntity<Map<String, Integer>> getLogCountByService() {
        return ResponseEntity.ok(logAggregationService.getLogCountByService());
    }

    /**
     * Get log count by level
     */
    @GetMapping("/stats/by-level")
    public ResponseEntity<Map<String, Integer>> getLogCountByLevel() {
        return ResponseEntity.ok(logAggregationService.getLogCountByLevel());
    }
}

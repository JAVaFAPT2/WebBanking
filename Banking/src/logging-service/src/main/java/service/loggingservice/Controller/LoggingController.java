package service.loggingservice.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.loggingservice.service.LoggingService;
import service.shared.models.Log;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/logs")
public class LoggingController {

    private final LoggingService loggingService;

    public LoggingController(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    /**
     * Simple endpoint to verify the logs endpoint is accessible.
     */
    @GetMapping
    public ResponseEntity<String> getLogs() {
        loggingService.logInfo("Received GET request for logs");
        return ResponseEntity.ok("Logs endpoint accessed at " + LocalDateTime.now());
    }

    /**
     * Retrieves a log entry by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getLogById(@PathVariable UUID id) {
        Log log = loggingService.getLog(id);
        if (log != null) {
            return ResponseEntity.ok(log);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Retrieves a log entry by its timestamp.
     * Note: The timestamp should be passed in a format that Spring can convert (for example, ISO-8601).
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<?> getLogByDate(@PathVariable LocalDateTime date) {
        Log log = loggingService.getLogByDate(date);
        if (log != null) {
            return ResponseEntity.ok(log);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Creates a new log entry with an INFO level.
     */
    @PostMapping
    public ResponseEntity<Log> createLog(@RequestBody String message) {
        loggingService.logInfo("Received POST request to create log");
        Log savedLog = loggingService.logInfo(message);
        return ResponseEntity.ok(savedLog);
    }
    /**
     * Creates a new log entry with an ERROR level.
     */
    @PostMapping("/error")
    public ResponseEntity<Log> createErrorLog(@RequestBody String message) {
        loggingService.logError("Received POST request to create error log");
        Log savedLog = loggingService.logError(message);
        return ResponseEntity.ok(savedLog);
    }

}

package service.monitorservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service responsible for aggregating, processing, and storing logs from all microservices.
 * Provides search capabilities and integration with the alerting system for log-based alerts.
 */
@Service
public class LogAggregationService {
    private static final Logger logger = LoggerFactory.getLogger(LogAggregationService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AlertService alertService;

    // In-memory log storage (in a production environment, this would be replaced with a database or ELK stack)
    private final List<LogEntry> recentLogs = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, Integer> errorCountByService = new ConcurrentHashMap<>();

    // Pattern to identify error messages
    private static final Pattern ERROR_PATTERN = Pattern.compile("(?i)(error|exception|fail|failed|failure)");

    // Maximum number of logs to keep in memory
    @Value("${log.max-entries}")
    private int maxLogEntries;

    // Log retention period in hours
    @Value("${log.retention-hours}")
    private int logRetentionHours;

    // Error threshold for alerting
    @Value("${log.error-threshold}")
    private int errorThreshold;

    // Topic to listen for logs
    @Value("${log.kafka.topic}")
    private String logTopic;

    // Topic to forward processed logs
    @Value("${log.kafka.processed-topic}")
    private String processedLogTopic;

    @Autowired
    public LogAggregationService(KafkaTemplate<String, String> kafkaTemplate, AlertService alertService) {
        this.kafkaTemplate = kafkaTemplate;
        this.alertService = alertService;
    }

    @PostConstruct
    public void init() {
        // Start a scheduled task to clean up old logs
        Timer cleanupTimer = new Timer(true);
        cleanupTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                cleanupOldLogs();
            }
        }, 3600000, 3600000); // Run every hour

        logger.info("Log aggregation service initialized with retention period of {} hours", logRetentionHours);
    }

    /**
     * Kafka listener for log messages from all services
     *
     * @param logMessage the log message received from Kafka
     */
    @KafkaListener(topics = "${log.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeLog(String logMessage) {
        try {
            // Parse the log message
            LogEntry logEntry = parseLogMessage(logMessage);

            // Store the log
            addLog(logEntry);

            // Forward processed log to another topic if needed
            forwardProcessedLog(logEntry);

            // Check for errors and potentially trigger alerts
            if (isErrorLog(logEntry)) {
                incrementErrorCount(logEntry.serviceName());
            }
        } catch (Exception e) {
            logger.error("Error processing log message: {}", e.getMessage(), e);
        }
    }

    /**
     * Forward processed log entry to another Kafka topic
     * This method uses the kafkaTemplate that was previously unused
     *
     * @param logEntry the processed log entry
     */
    private void forwardProcessedLog(LogEntry logEntry) {
        try {
            // Only forward logs of certain levels (e.g., ERROR, WARN) to reduce volume
            if (isSignificantLog(logEntry)) {
                String formattedLog = logEntry.toString();
                kafkaTemplate.send(processedLogTopic, logEntry.serviceName(), formattedLog);
                logger.debug("Forwarded processed log to topic {}: {}", processedLogTopic, formattedLog);
            }
        } catch (Exception e) {
            logger.error("Failed to forward processed log: {}", e.getMessage(), e);
        }
    }

    /**
     * Determine if a log is significant enough to forward
     *
     * @param logEntry the log entry to check
     * @return true if the log is significant
     */
    private boolean isSignificantLog(LogEntry logEntry) {
        String level = logEntry.level().toUpperCase();
        return level.equals("ERROR") || level.equals("WARN") || level.equals("FATAL") ||
                level.equals("SEVERE");
    }

    /**
     * Manually send a log entry to the log topic
     * This provides another use of the kafkaTemplate
     *
     * @param serviceName the name of the service
     * @param level the log level
     * @param message the log message
     */
    public void sendLog(String serviceName, String level, String message) {
        try {
            LogEntry logEntry = new LogEntry(serviceName, LocalDateTime.now(), level, message);
            String formattedLog = logEntry.toString();

            // Send to Kafka
            kafkaTemplate.send(logTopic, serviceName, formattedLog);

            // Also add to local storage
            addLog(logEntry);

            logger.debug("Manually sent log to topic {}: {}", logTopic, formattedLog);
        } catch (Exception e) {
            logger.error("Failed to send log: {}", e.getMessage(), e);
        }
    }

    /**
     * Parse a log message into a structured LogEntry
     *
     * @param logMessage the raw log message
     * @return a structured LogEntry object
     */
    private LogEntry parseLogMessage(String logMessage) {
        // Example format: [SERVICE_NAME] [TIMESTAMP] [LOG_LEVEL] - Message
        // This is a simplified parser - in a real system, you might use a more robust approach

        try {
            String[] parts = logMessage.split("\\s+", 4);
            if (parts.length >= 4) {
                String serviceName = parts[0].replace("[", "").replace("]", "");
                LocalDateTime timestamp = LocalDateTime.parse(parts[1].replace("[", "").replace("]", ""),
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                String level = parts[2].replace("[", "").replace("]", "");
                String message = parts[3];

                return new LogEntry(serviceName, timestamp, level, message);
            }
        } catch (Exception e) {
            logger.warn("Could not parse log message using standard format: {}", logMessage);
        }

        // Fallback for non-standard format
        return new LogEntry("unknown", LocalDateTime.now(), "INFO", logMessage);
    }

    /**
     * Add a log entry to the in-memory storage
     *
     * @param logEntry the log entry to add
     */
    public void addLog(LogEntry logEntry) {
        synchronized (recentLogs) {
            recentLogs.add(logEntry);

            // Trim the log list if it exceeds the maximum size
            if (recentLogs.size() > maxLogEntries) {
                recentLogs.removeFirst();  // Remove the oldest entry
            }
        }
    }

    /**
     * Check if a log entry contains error indicators
     *
     * @param logEntry the log entry to check
     * @return true if the log entry indicates an error
     */
    private boolean isErrorLog(LogEntry logEntry) {
        if (logEntry.level().equalsIgnoreCase("ERROR") ||
                logEntry.level().equalsIgnoreCase("SEVERE") ||
                logEntry.level().equalsIgnoreCase("FATAL")) {
            return true;
        }

        Matcher matcher = ERROR_PATTERN.matcher(logEntry.message());
        return matcher.find();
    }

    /**
     * Increment the error count for a service and check if it exceeds the threshold
     *
     * @param serviceName the name of the service
     */
    private void incrementErrorCount(String serviceName) {
        int count = errorCountByService.getOrDefault(serviceName, 0) + 1;
        errorCountByService.put(serviceName, count);

        // Check if we need to trigger an alert
        if (count >= errorThreshold) {
            String alertMessage = String.format("High error rate detected in service %s: %d errors in the last period",
                    serviceName, count);
            alertService.sendCriticalAlert(serviceName, alertMessage);

            // Reset the counter after alerting
            errorCountByService.put(serviceName, 0);
        }
    }

    /**
     * Clean up logs older than the retention period
     */
    private void cleanupOldLogs() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(logRetentionHours);

        synchronized (recentLogs) {
            recentLogs.removeIf(logEntry -> logEntry.timestamp().isBefore(cutoffTime));
        }

        logger.debug("Cleaned up logs older than {}", cutoffTime);
    }

    /**
     * Search logs by various criteria
     *
     * @param serviceName optional service name filter
     * @param level optional log level filter
     * @param searchText optional text to search for in log messages
     * @param startTime optional start time for the search period
     * @param endTime optional end time for the search period
     * @return a list of matching log entries
     */
    public List<LogEntry> searchLogs(String serviceName, String level, String searchText,
                                     LocalDateTime startTime, LocalDateTime endTime) {
        List<LogEntry> results = new ArrayList<>();

        synchronized (recentLogs) {
            for (LogEntry entry : recentLogs) {
                // Apply filters
                if (serviceName != null && !entry.serviceName().equalsIgnoreCase(serviceName)) {
                    continue;
                }

                if (level != null && !entry.level().equalsIgnoreCase(level)) {
                    continue;
                }

                if (searchText != null && !entry.message().toLowerCase().contains(searchText.toLowerCase())) {
                    continue;
                }

                if (startTime != null && entry.timestamp().isBefore(startTime)) {
                    continue;
                }

                if (endTime != null && entry.timestamp().isAfter(endTime)) {
                    continue;
                }

                // All filters passed, add to results
                results.add(entry);
            }
        }

        // Sort by timestamp (newest first)
        results.sort(Comparator.comparing(LogEntry::timestamp).reversed());

        return results;
    }

    /**
     * Get recent logs for a specific service
     *
     * @param serviceName the name of the service
     * @param limit maximum number of logs to return
     * @return a list of recent logs for the service
     */
    public List<LogEntry> getRecentLogsForService(String serviceName, int limit) {
        return searchLogs(serviceName, null, null, null, null).stream()
                .limit(limit)
                .toList();
    }

    /**
     * Get recent error logs across all services
     *
     * @param limit maximum number of logs to return
     * @return a list of recent error logs
     */
    public List<LogEntry> getRecentErrorLogs(int limit) {
        return searchLogs(null, "ERROR", null, null, null).stream()
                .limit(limit)
                .toList();
    }

    /**
     * Get statistics about log distribution by service
     *
     * @return a map of service names to log counts
     */
    public Map<String, Integer> getLogCountByService() {
        Map<String, Integer> counts = new HashMap<>();

        synchronized (recentLogs) {
            for (LogEntry entry : recentLogs) {
                String serviceName = entry.serviceName();
                counts.put(serviceName, counts.getOrDefault(serviceName, 0) + 1);
            }
        }

        return counts;
    }

    /**
     * Get statistics about log distribution by level
     *
     * @return a map of log levels to log counts
     */
    public Map<String, Integer> getLogCountByLevel() {
        Map<String, Integer> counts = new HashMap<>();

        synchronized (recentLogs) {
            for (LogEntry entry : recentLogs) {
                String level = entry.level();
                counts.put(level, counts.getOrDefault(level, 0) + 1);
            }
        }

        return counts;
    }
    /**
     * Aggregate logs from a service with the provided content
     *
     * @param serviceId the ID of the service
     * @param logContent the log content
     */
    public void aggregateLog(String serviceId, String logContent) {
        // Create a new LogEntry with the current timestamp and default level of INFO
        LogEntry logEntry = new LogEntry(serviceId, LocalDateTime.now(), "INFO", logContent);

        // Add the log entry to storage
        addLog(logEntry);

        // Check if this is an error log and handle accordingly
        if (isErrorLog(logEntry)) {
            incrementErrorCount(serviceId);
        }

        // Forward significant logs
        if (isSignificantLog(logEntry)) {
            forwardProcessedLog(logEntry);
        }

        logger.debug("Aggregated log for service {}: {}", serviceId, logContent);
    }


    /**
         * Class representing a structured log entry
         */
        public record LogEntry(String serviceName, LocalDateTime timestamp, String level, String message) {

        @Override
            public String toString() {
                return String.format("[%s] [%s] [%s] - %s",
                        serviceName,
                        timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        level,
                        message);
            }
        }
}

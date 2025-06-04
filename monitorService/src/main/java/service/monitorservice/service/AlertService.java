package service.monitorservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import service.monitorservice.model.AlertSeverity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsible for generating and sending alerts based on system metrics and events.
 * Supports multiple notification channels including email and Kafka events.
 */
@Service
public class AlertService {
    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JavaMailSender emailSender;

    @Value("${alert.kafka.topic:system-alerts}")
    private String alertTopic;

    @Value("${alert.email.recipients:admin@webbanking.com}")
    private String alertRecipients;

    @Value("${alert.email.from:alerts@webbanking.com}")
    private String alertFromEmail;

    // Store alert thresholds for different metrics
    private final Map<String, Double> alertThresholds = new HashMap<>();

    // Track alert status to prevent alert storms (key: alertId, value: last alert time)
    private final Map<String, LocalDateTime> alertStatus = new ConcurrentHashMap<>();

    // Cooldown period between repeated alerts in minutes
    @Value("${alert.cooldown.minutes:15}")
    private int alertCooldownMinutes;

    @Autowired
    public AlertService(KafkaTemplate<String, String> kafkaTemplate,
                        JavaMailSender emailSender) {
        this.kafkaTemplate = kafkaTemplate;
        this.emailSender = emailSender;

        // Initialize default thresholds
        initializeDefaultThresholds();
    }

    private void initializeDefaultThresholds() {
        alertThresholds.put("cpu.usage", 85.0);  // CPU usage percentage
        alertThresholds.put("memory.usage", 90.0);  // Memory usage percentage
        alertThresholds.put("disk.usage", 85.0);  // Disk usage percentage
        alertThresholds.put("api.error.rate", 5.0);  // Error rate percentage
        alertThresholds.put("api.response.time", 2000.0);  // Response time in ms
        alertThresholds.put("transaction.failure.rate", 2.0);  // Transaction failure rate percentage
    }

    /**
     * Update an alert threshold for a specific metric
     *
     * @param metricName the name of the metric
     * @param threshold the new threshold value
     */
    public void updateAlertThreshold(String metricName, Double threshold) {
        alertThresholds.put(metricName, threshold);
        logger.info("Updated alert threshold for {}: {}", metricName, threshold);
    }

    /**
     * Check if a metric value exceeds its threshold and trigger an alert if needed
     *
     * @param metricName the name of the metric
     * @param value the current value of the metric
     * @param serviceId the service identifier that generated the metric
     * @return true if an alert was triggered
     */
    public boolean checkAndAlert(String metricName, Double value, String serviceId) {
        if (!alertThresholds.containsKey(metricName)) {
            logger.warn("No threshold defined for metric: {}", metricName);
            return false;
        }

        Double threshold = alertThresholds.get(metricName);
        if (value > threshold) {
            String alertId = serviceId + ":" + metricName;

            // Check if we're in cooldown period for this alert
            if (isInCooldownPeriod(alertId)) {
                logger.debug("Alert for {} is in cooldown period, skipping", alertId);
                return false;
            }

            // Generate and send the alert
            String alertMessage = String.format("ALERT: %s from %s exceeded threshold. Current value: %.2f, Threshold: %.2f",
                    metricName, serviceId, value, threshold);

            sendAlert(alertId, alertMessage, AlertSeverity.WARNING);
            return true;
        }

        return false;
    }

    /**
     * Send a critical system alert
     *
     * @param serviceId the service identifier
     * @param message the alert message
     */
    public void sendCriticalAlert(String serviceId, String message) {
        String alertId = serviceId + ":critical:" + System.currentTimeMillis();
        sendAlert(alertId, "CRITICAL ALERT from " + serviceId + ": " + message, AlertSeverity.CRITICAL);
    }

    /**
     * Send an informational alert
     *
     * @param serviceId the service identifier
     * @param message the alert message
     */
    public void sendInfoAlert(String serviceId, String message) {
        String alertId = serviceId + ":info:" + System.currentTimeMillis();
        sendAlert(alertId, "INFO from " + serviceId + ": " + message, AlertSeverity.INFO);
    }

    public void sendAlert(String alertId, String message, AlertSeverity severity) {
        logger.info("Sending alert: {} - {}", alertId, message);

        // Update alert status with current time
        alertStatus.put(alertId, LocalDateTime.now());

        // Send to Kafka
        try {
            kafkaTemplate.send(alertTopic, alertId, severity.name() + ": " + message);
        } catch (Exception e) {
            logger.error("Failed to send alert to Kafka: {}", e.getMessage(), e);
        }

        // Send email for WARNING and CRITICAL alerts
        if (severity == AlertSeverity.WARNING || severity == AlertSeverity.CRITICAL) {
            sendEmailAlert(message, severity);
        }
    }

    private void sendEmailAlert(String message, AlertSeverity severity) {
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setFrom(alertFromEmail);
            email.setTo(alertRecipients.split(","));
            email.setSubject("WebBanking " + severity.name() + " Alert");
            email.setText(message + "\n\nTime: " + LocalDateTime.now());

            emailSender.send(email);
        } catch (Exception e) {
            logger.error("Failed to send email alert: {}", e.getMessage(), e);
        }
    }

    private boolean isInCooldownPeriod(String alertId) {
        if (!alertStatus.containsKey(alertId)) {
            return false;
        }

        LocalDateTime lastAlertTime = alertStatus.get(alertId);
        LocalDateTime cooldownEndTime = lastAlertTime.plusMinutes(alertCooldownMinutes);

        return LocalDateTime.now().isBefore(cooldownEndTime);
    }

    /**
     * Clear alert history for testing or maintenance purposes
     */
    public void clearAlertHistory() {
        alertStatus.clear();
        logger.info("Alert history cleared");
    }


}

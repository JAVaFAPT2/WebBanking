package service.messagebroker.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import service.messagebroker.service.MetricsService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of the MetricsService
 * Records application metrics for monitoring and analysis
 */
@Service
public class MetricsServiceImpl implements MetricsService {
    private static final Logger logger = LoggerFactory.getLogger(MetricsServiceImpl.class);

    // Store counters in memory (in a real app, these would go to a metrics system)
    private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();

    // Store gauge values in memory
    private final Map<String, Double> gauges = new ConcurrentHashMap<>();

    // Store timing metrics
    private final Map<String, AtomicLong> timingTotals = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> timingCounts = new ConcurrentHashMap<>();

    @Value("${app.metrics.enabled:true}")
    private boolean metricsEnabled;

    @Value("${app.metrics.log-level:INFO}")
    private String metricsLogLevel;

    @Override
    public void recordRiskAssessmentDuration(long durationMillis) {
        if (!metricsEnabled) return;

        // Record the timing metric
        timingTotals.computeIfAbsent("risk.assessment.duration", k -> new AtomicLong(0))
                .addAndGet(durationMillis);
        timingCounts.computeIfAbsent("risk.assessment.duration", k -> new AtomicLong(0))
                .incrementAndGet();

        // Calculate and record the average
        long total = timingTotals.get("risk.assessment.duration").get();
        long count = timingCounts.get("risk.assessment.duration").get();
        double average = count > 0 ? (double) total / count : 0;

        gauges.put("risk.assessment.duration.avg", average);

        // Log the metric
        logMetric("Risk assessment duration", durationMillis + "ms", "avg=" + String.format("%.2f", average) + "ms");
    }

    @Override
    public void recordTransactionProcessed(String transactionType, boolean isSuccessful) {
        if (!metricsEnabled) return;

        // Increment total counter
        incrementCounter("transaction.processed.total");

        // Increment type-specific counter
        incrementCounter("transaction.processed." + transactionType.toLowerCase());

        // Increment success/failure counter
        if (isSuccessful) {
            incrementCounter("transaction.processed.success");
        } else {
            incrementCounter("transaction.processed.failure");
        }

        // Log the metric
        logMetric("Transaction processed",
                transactionType,
                "success=" + isSuccessful);
    }

    @Override
    public void recordFraudDetection(String fraudType, String severity) {
        if (!metricsEnabled) return;

        // Increment total counter
        incrementCounter("fraud.detected.total");

        // Increment type-specific counter
        incrementCounter("fraud.detected." + fraudType.toLowerCase());

        // Increment severity-specific counter
        incrementCounter("fraud.detected.severity." + severity.toLowerCase());

        // Log the metric
        logMetric("Fraud detected",
                fraudType,
                "severity=" + severity);
    }

    @Override
    public void incrementCounter(String metricName) {
        if (!metricsEnabled) return;

        counters.computeIfAbsent(metricName, k -> new AtomicLong(0))
                .incrementAndGet();
    }

    @Override
    public void recordGaugeValue(String metricName, double value) {
        if (!metricsEnabled) return;

        gauges.put(metricName, value);

        // Log the metric
        logMetric("Gauge", metricName, "value=" + value);
    }

    /**
     * Logs a metric based on the configured log level
     */
    private void logMetric(String metricType, String metricName, String metricValue) {
        String message = String.format("METRIC - %s: %s (%s)", metricType, metricName, metricValue);

        switch (metricsLogLevel.toUpperCase()) {
            case "DEBUG":
                logger.debug(message);
                break;
            case "TRACE":
                logger.trace(message);
                break;
            case "WARN":
                logger.warn(message);
                break;
            case "ERROR":
                logger.error(message);
                break;
            case "INFO":
            default:
                logger.info(message);
                break;
        }
    }

    /**
     * Gets the current value of a counter
     *
     * @param metricName The name of the counter
     * @return The current value
     */
    public long getCounterValue(String metricName) {
        AtomicLong counter = counters.get(metricName);
        return counter != null ? counter.get() : 0;
    }

    /**
     * Gets the current value of a gauge
     *
     * @param metricName The name of the gauge
     * @return The current value
     */
    public double getGaugeValue(String metricName) {
        return gauges.getOrDefault(metricName, 0.0);
    }

    /**
     * Resets all metrics
     * Useful for testing or when metrics need to be cleared
     */
    public void resetAllMetrics() {
        counters.clear();
        gauges.clear();
        timingTotals.clear();
        timingCounts.clear();
        logger.info("All metrics have been reset");
    }
}

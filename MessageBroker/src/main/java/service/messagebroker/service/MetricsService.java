package service.messagebroker.service;

/**
 * Service for recording application metrics
 * Used for monitoring performance, usage patterns, and system health
 */
public interface MetricsService {

    /**
     * Records the duration of a risk assessment operation
     *
     * @param durationMillis The duration in milliseconds
     */
    void recordRiskAssessmentDuration(long durationMillis);

    /**
     * Records a transaction processing event
     *
     * @param transactionType The type of transaction
     * @param isSuccessful Whether the processing was successful
     */
    void recordTransactionProcessed(String transactionType, boolean isSuccessful);

    /**
     * Records a fraud detection event
     *
     * @param fraudType The type of fraud detected
     * @param severity The severity level
     */
    void recordFraudDetection(String fraudType, String severity);

    /**
     * Increments a counter for a specific metric
     *
     * @param metricName The name of the metric
     */
    void incrementCounter(String metricName);

    /**
     * Records a gauge value for a specific metric
     *
     * @param metricName The name of the metric
     * @param value The value to record
     */
    void recordGaugeValue(String metricName, double value);
}

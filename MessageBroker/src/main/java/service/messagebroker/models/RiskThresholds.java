package service.messagebroker.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Defines threshold values for different risk levels and transaction types
 * Used for determining appropriate actions based on risk scores
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskThresholds {

    /**
     * Threshold for high risk transactions
     */
    private double highRiskThreshold;



    /**
     * Threshold for transactions requiring review
     */
    private double reviewThreshold;

    /**
     * Threshold for transactions requiring monitoring
     */
    private double monitorThreshold;

    /**
     * Threshold for blocking transactions
     */
    private double blockThreshold;

    /**
     * Threshold for anomaly detection requiring monitoring
     */
    private double monitorAnomalyThreshold;

    /**
     * Threshold for anomaly detection requiring review
     */
    private double reviewAnomalyThreshold;

    /**
     * Threshold for anomaly detection requiring blocking
     */
    private double blockAnomalyThreshold;

    /**
     * Special threshold for international wire transfers
     */
    private double internationalWireThreshold;

    /**
     * Creates default thresholds for standard transactions
     * @return Default risk thresholds
     */
    public static RiskThresholds getDefaultThresholds() {
        return RiskThresholds.builder()
                .highRiskThreshold(7.0)
                .reviewThreshold(5.0)
                .monitorThreshold(3.0)
                .blockThreshold(8.5)
                .monitorAnomalyThreshold(0.3)
                .reviewAnomalyThreshold(0.6)
                .blockAnomalyThreshold(0.8)
                .internationalWireThreshold(6.0)
                .build();
    }

    /**
     * Creates thresholds for high-risk transaction types
     * @return High-risk transaction thresholds
     */
    public static RiskThresholds getHighRiskThresholds() {
        return RiskThresholds.builder()
                .highRiskThreshold(6.0)
                .reviewThreshold(4.0)
                .monitorThreshold(2.0)
                .blockThreshold(7.5)
                .monitorAnomalyThreshold(0.25)
                .reviewAnomalyThreshold(0.5)
                .blockAnomalyThreshold(0.7)
                .internationalWireThreshold(5.0)
                .build();
    }

    /**
     * Creates thresholds for low-risk transaction types
     * @return Low-risk transaction thresholds
     */
    public static RiskThresholds getLowRiskThresholds() {
        return RiskThresholds.builder()
                .highRiskThreshold(8.0)
                .reviewThreshold(6.0)
                .monitorThreshold(4.0)
                .blockThreshold(9.0)
                .monitorAnomalyThreshold(0.4)
                .reviewAnomalyThreshold(0.7)
                .blockAnomalyThreshold(0.9)
                .internationalWireThreshold(7.0)
                .build();
    }
    /**
     * Gets the general anomaly threshold
     * This is a convenience method that returns the review anomaly threshold
     * @return The general anomaly threshold
     */
    public double getAnomalyThreshold() {
        // Using the review anomaly threshold as the general anomaly threshold
        // This is a reasonable default since "review" is a middle-ground action
        return reviewAnomalyThreshold;
    }

}

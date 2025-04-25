package service.messagebroker.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model representing the result of an anomaly detection analysis
 * Contains information about detected anomalies in transaction patterns
 */
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class AnomalyDetectionResult {

    /**
     * Overall anomaly score (0.0 to 1.0)
     * Higher values indicate more anomalous behavior
     */
    private double score;

    /**
     * Confidence level in the anomaly detection (0.0 to 1.0)
     */
    private double confidence;

    /**
     * List of detected anomaly types
     */

    private List<String> detectedAnomalies = new ArrayList<>();

    /**
     * Map of individual feature scores that contributed to the overall score
     */

    private Map<String, Double> featureScores = new HashMap<>();

    /**
     * Timestamp when the analysis was performed
     */
    private Instant analysisTimestamp;

    /**
     * The algorithm or model used for detection
     */
    private String detectionMethod;

    /**
     * Explanation or reason for the anomaly score
     */
    private String explanation;

    /**
     * Recommended action based on the anomaly detection
     */
    private String recommendedAction;

    /**
     * Creates a default result with no anomalies detected
     *
     * @return A default anomaly detection result
     */
    public static AnomalyDetectionResult createNormalResult() {
        return AnomalyDetectionResult.builder()
                .score(0.0)
                .confidence(0.95)
                .analysisTimestamp(Instant.now())
                .detectionMethod("DEFAULT")
                .explanation("No anomalies detected")
                .recommendedAction("PROCEED")
                .build();
    }

    /**
     * Creates a result for a detected anomaly
     *
     * @param score The anomaly score
     * @param anomalyType The type of anomaly detected
     * @param explanation An explanation of the anomaly
     * @return An anomaly detection result
     */
    public static AnomalyDetectionResult createAnomalyResult(double score, String anomalyType, String explanation) {
        List<String> anomalies = new ArrayList<>();
        anomalies.add(anomalyType);

        String action = determineAction(score);

        return AnomalyDetectionResult.builder()
                .score(score)
                .confidence(0.8)
                .detectedAnomalies(anomalies)
                .analysisTimestamp(Instant.now())
                .detectionMethod("STATISTICAL")
                .explanation(explanation)
                .recommendedAction(action)
                .build();
    }

    /**
     * Determines the recommended action based on the anomaly score
     *
     * @param score The anomaly score
     * @return The recommended action
     */
    private static String determineAction(double score) {
        if (score > 0.8) {
            return "BLOCK";
        } else if (score > 0.6) {
            return "REVIEW";
        } else if (score > 0.3) {
            return "MONITOR";
        } else {
            return "PROCEED";
        }
    }

    /**
     * Adds a feature score to the result
     *
     * @param featureName The name of the feature
     * @param featureScore The anomaly score for this feature
     */
    public void addFeatureScore(String featureName, double featureScore) {
        if (featureScores == null) {
            featureScores = new HashMap<>();
        }
        featureScores.put(featureName, featureScore);
    }

    /**
     * Adds a detected anomaly to the result
     *
     * @param anomalyType The type of anomaly detected
     */
    public void addDetectedAnomaly(String anomalyType) {
        if (detectedAnomalies == null) {
            detectedAnomalies = new ArrayList<>();
        }
        detectedAnomalies.add(anomalyType);
    }

    /**
     * Checks if any anomalies were detected
     *
     * @return True if anomalies were detected, false otherwise
     */
    public boolean hasAnomalies() {
        return score > 0.1 && (detectedAnomalies != null && !detectedAnomalies.isEmpty());
    }

    /**
     * Gets the severity level based on the anomaly score
     *
     * @return The severity level as a string
     */
    public String getSeverityLevel() {
        if (score > 0.8) {
            return "CRITICAL";
        } else if (score > 0.6) {
            return "HIGH";
        } else if (score > 0.3) {
            return "MEDIUM";
        } else if (score > 0.1) {
            return "LOW";
        } else {
            return "NONE";
        }
    }
    /**
     * Creates a new builder for AnomalyDetectionResult
     *
     * @return A new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for AnomalyDetectionResult
     */
    public static class Builder {
        private double score;
        private double confidence;
        private List<String> detectedAnomalies = new ArrayList<>();
        private Map<String, Double> featureScores = new HashMap<>();
        private Instant analysisTimestamp;
        private String detectionMethod;
        private String explanation;
        private String recommendedAction;

        /**
         * Sets the anomaly score
         *
         * @param score The anomaly score
         * @return This builder
         */
        public Builder score(double score) {
            this.score = score;
            return this;
        }

        /**
         * Sets the confidence level
         *
         * @param confidence The confidence level
         * @return This builder
         */
        public Builder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        /**
         * Sets the detected anomalies
         *
         * @param detectedAnomalies The detected anomalies
         * @return This builder
         */
        public Builder detectedAnomalies(List<String> detectedAnomalies) {
            this.detectedAnomalies = detectedAnomalies;
            return this;
        }

        /**
         * Adds a detected anomaly
         *
         * @param anomaly The anomaly to add
         * @return This builder
         */
        public Builder addDetectedAnomaly(String anomaly) {
            this.detectedAnomalies.add(anomaly);
            return this;
        }

        /**
         * Sets the feature scores
         *
         * @param featureScores The feature scores
         * @return This builder
         */
        public Builder featureScores(Map<String, Double> featureScores) {
            this.featureScores = featureScores;
            return this;
        }

        /**
         * Sets the analysis timestamp
         *
         * @param analysisTimestamp The analysis timestamp
         * @return This builder
         */
        public Builder analysisTimestamp(Instant analysisTimestamp) {
            this.analysisTimestamp = analysisTimestamp;
            return this;
        }

        /**
         * Sets the detection method
         *
         * @param detectionMethod The detection method
         * @return This builder
         */
        public Builder detectionMethod(String detectionMethod) {
            this.detectionMethod = detectionMethod;
            return this;
        }

        /**
         * Sets the explanation
         *
         * @param explanation The explanation
         * @return This builder
         */
        public Builder explanation(String explanation) {
            this.explanation = explanation;
            return this;
        }

        /**
         * Sets the recommended action
         *
         * @param recommendedAction The recommended action
         * @return This builder
         */
        public Builder recommendedAction(String recommendedAction) {
            this.recommendedAction = recommendedAction;
            return this;
        }

        /**
         * Builds the AnomalyDetectionResult
         *
         * @return The built AnomalyDetectionResult
         */
        public AnomalyDetectionResult build() {
            AnomalyDetectionResult result = new AnomalyDetectionResult();
            result.score = this.score;
            result.confidence = this.confidence;
            result.detectedAnomalies = this.detectedAnomalies;
            result.featureScores = this.featureScores;
            result.analysisTimestamp = this.analysisTimestamp;
            result.detectionMethod = this.detectionMethod;
            result.explanation = this.explanation;
            result.recommendedAction = this.recommendedAction;
            return result;
        }
    }
}


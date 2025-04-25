package service.messagebroker.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response model for risk assessment operations
 * Contains the results of machine learning or rules-based risk evaluation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessmentResponse {

    /**
     * The calculated risk score (0-10 scale)
     * Higher values indicate higher risk
     */
    private double riskScore;

    /**
     * Version of the risk model used for assessment
     */
    private String modelVersion;

    /**
     * Confidence score of the risk assessment (0-1 scale)
     * Indicates how confident the model is in its risk evaluation
     */
    private double confidenceScore;

    /**
     * Reason codes explaining factors that contributed to the risk score
     */
    private String[] reasonCodes;

    /**
     * Recommended action based on the risk assessment
     * (e.g., "APPROVE", "REVIEW", "DENY")
     */
    private String recommendedAction;

    /**
     * Additional context or metadata about the risk assessment
     */
    private String assessmentContext;

    /**
     * Flag indicating if this was a fallback assessment
     * (e.g., when ML service is unavailable)
     */
    private boolean fallbackAssessment;
}

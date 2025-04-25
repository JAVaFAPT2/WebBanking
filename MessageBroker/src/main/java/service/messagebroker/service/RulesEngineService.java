package service.messagebroker.service;

import service.messagebroker.models.RiskAssessmentResponse;
import service.messagebroker.request.RiskAssessmentRequest;

/**
 * Service for evaluating risk using rule-based approaches
 * Provides a fallback mechanism when ML-based risk assessment is unavailable
 */
public interface RulesEngineService {

    /**
     * Evaluates transaction risk using predefined business rules
     *
     * @param request The risk assessment request
     * @return A risk assessment response with scores and recommendations
     */
    RiskAssessmentResponse evaluateRiskWithRules(RiskAssessmentRequest request);

    /**
     * Checks if a transaction violates any compliance rules
     *
     * @param request The risk assessment request
     * @return True if the transaction violates compliance rules, false otherwise
     */
    boolean checkComplianceViolations(RiskAssessmentRequest request);

    /**
     * Gets the reason codes for a rule-based risk assessment
     *
     * @param request The risk assessment request
     * @return An array of reason codes explaining the risk factors
     */
    String[] getRiskReasonCodes(RiskAssessmentRequest request);
}

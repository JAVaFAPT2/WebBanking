package service.messagebroker.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import service.messagebroker.models.RiskAssessmentResponse;
import service.messagebroker.request.RiskAssessmentRequest;
import service.messagebroker.service.RulesEngineService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of the RulesEngineService
 * Provides rule-based risk assessment as a fallback for ML-based assessment
 */
@Service
public class RulesEngineServiceImpl implements RulesEngineService {
    private static final Logger logger = LoggerFactory.getLogger(RulesEngineServiceImpl.class);

    @Value("${app.rules.high-value-threshold:10000.0}")
    private double highValueThreshold;

    @Value("${app.rules.medium-value-threshold:5000.0}")
    private double mediumValueThreshold;

    @Value("${app.rules.international-risk-factor:2.0}")
    private double internationalRiskFactor;

    @Value("${app.rules.new-device-risk-factor:1.5}")
    private double newDeviceRiskFactor;

    @Value("${app.rules.high-risk-countries}")
    private List<String> highRiskCountries;

    @Value("${app.rules.high-risk-transaction-types}")
    private List<String> highRiskTransactionTypes;

    @Value("${app.rules.model-version:rules-engine-v1.0}")
    private String modelVersion;

    @Override
    public RiskAssessmentResponse evaluateRiskWithRules(RiskAssessmentRequest request) {
        logger.info("Performing rule-based risk assessment for account: {}", request.getAccountId());

        try {
            // Calculate base risk score based on amount
            double amount = request.getTransactionAmount();
            double baseRiskScore = calculateBaseRiskScore(amount);

            // Apply risk factors based on transaction attributes
            double locationRiskFactor = calculateLocationRiskFactor(request.getLocation());
            double transactionTypeRiskFactor = calculateTransactionTypeRiskFactor(request.getTransactionType());
            double deviceRiskFactor = calculateDeviceRiskFactor(request.getDeviceInfo());

            // Calculate final risk score
            double finalRiskScore = Math.min(
                    baseRiskScore * locationRiskFactor * transactionTypeRiskFactor * deviceRiskFactor,
                    10.0);

            // Generate reason codes
            String[] reasonCodes = getRiskReasonCodes(request);

            // Determine recommended action
            String recommendedAction = determineRecommendedAction(finalRiskScore);

            // Build and return the response
            return RiskAssessmentResponse.builder()
                    .riskScore(finalRiskScore)
                    .modelVersion(modelVersion)
                    .confidenceScore(0.9) // Rules are deterministic, so high confidence
                    .reasonCodes(reasonCodes)
                    .recommendedAction(recommendedAction)
                    .assessmentContext("RULES_ENGINE")
                    .fallbackAssessment(true)
                    .build();
        } catch (Exception e) {
            logger.error("Error in rule-based risk assessment: {}", e.getMessage());

            // Return a default moderate risk assessment in case of errors
            return createDefaultRiskAssessment(request.getAccountId());
        }
    }

    @Override
    public boolean checkComplianceViolations(RiskAssessmentRequest request) {
        // Check for compliance violations based on various rules

        // Check for transactions to sanctioned countries
        if (isHighRiskCountry(request.getLocation())) {
            logger.warn("Compliance violation: transaction to high-risk country {}", request.getLocation());
            return true;
        }

        // Check for unusually large transactions
        if (request.getTransactionAmount() > highValueThreshold * 2) {
            logger.warn("Compliance violation: unusually large transaction amount {}",
                    request.getTransactionAmount());
            return true;
        }

        // Check for high-risk transaction types
        if (isHighRiskTransactionType(request.getTransactionType())) {
            logger.warn("Compliance violation: high-risk transaction type {}",
                    request.getTransactionType());
            return true;
        }

        return false;
    }

    @Override
    public String[] getRiskReasonCodes(RiskAssessmentRequest request) {
        List<String> reasonCodes = new ArrayList<>();

        // Add reason codes based on transaction attributes
        double amount = request.getTransactionAmount();

        // Amount-based reason codes
        if (amount > highValueThreshold) {
            reasonCodes.add("HIGH_VALUE_TRANSACTION");
        } else if (amount > mediumValueThreshold) {
            reasonCodes.add("MEDIUM_VALUE_TRANSACTION");
        }

        // Location-based reason codes
        if ("INTERNATIONAL".equals(request.getLocation())) {
            reasonCodes.add("INTERNATIONAL_TRANSACTION");
        }

        if (isHighRiskCountry(request.getLocation())) {
            reasonCodes.add("HIGH_RISK_COUNTRY");
        }

        // Transaction type reason codes
        if (isHighRiskTransactionType(request.getTransactionType())) {
            reasonCodes.add("HIGH_RISK_TRANSACTION_TYPE");
        }

        // Device-related reason codes
        if (request.getDeviceInfo() != null && request.getDeviceInfo().toString().contains("new")) {
            reasonCodes.add("NEW_DEVICE");
        }

        // If no specific reasons, add a default
        if (reasonCodes.isEmpty()) {
            reasonCodes.add("STANDARD_RISK_ASSESSMENT");
        }

        return reasonCodes.toArray(new String[0]);
    }

    /**
     * Calculates the base risk score based on transaction amount
     */
    private double calculateBaseRiskScore(double amount) {
        if (amount > highValueThreshold) {
            return 5.0 + (amount - highValueThreshold) / 2000.0;
        } else if (amount > mediumValueThreshold) {
            return 3.0 + (amount - mediumValueThreshold) / 1000.0;
        } else {
            return Math.max(1.0, amount / 1000.0);
        }
    }

    /**
     * Calculates risk factor based on transaction location
     */
    private double calculateLocationRiskFactor(String location) {
        if (location == null) {
            return 1.0;
        }

        if (isHighRiskCountry(location)) {
            return 2.5;
        } else if ("INTERNATIONAL".equals(location)) {
            return internationalRiskFactor;
        } else if ("HIGH_RISK".equals(location)) {
            return 2.0;
        } else {
            return 1.0;
        }
    }

    /**
     * Calculates risk factor based on transaction type
     */
    private double calculateTransactionTypeRiskFactor(String transactionType) {
        if (transactionType == null) {
            return 1.0;
        }

        if (isHighRiskTransactionType(transactionType)) {
            return 2.0;
        } else if ("WIRE_TRANSFER".equals(transactionType)) {
            return 1.5;
        } else if ("CASH_ADVANCE".equals(transactionType)) {
            return 1.8;
        } else {
            return 1.0;
        }
    }

    /**
     * Calculates risk factor based on device information
     */
    private double calculateDeviceRiskFactor(Object deviceInfo) {
        if (deviceInfo == null) {
            return 1.0;
        }

        String deviceInfoStr = deviceInfo.toString();

        if (deviceInfoStr.contains("new")) {
            return newDeviceRiskFactor;
        } else if (deviceInfoStr.contains("unknown")) {
            return 1.8;
        } else {
            return 1.0;
        }
    }

    /**
     * Determines if a location is a high-risk country
     */
    private boolean isHighRiskCountry(String location) {
        if (location == null || highRiskCountries == null) {
            return false;
        }

        return highRiskCountries.contains(location);
    }

    /**
     * Determines if a transaction type is high-risk
     */
    private boolean isHighRiskTransactionType(String transactionType) {
        if (transactionType == null || highRiskTransactionTypes == null) {
            return false;
        }

        return highRiskTransactionTypes.contains(transactionType);
    }

    /**
     * Determines the recommended action based on risk score
     */
    private String determineRecommendedAction(double riskScore) {
        if (riskScore < 3.0) {
            return "APPROVE";
        } else if (riskScore < 6.0) {
            return "REVIEW";
        } else if (riskScore < 8.0) {
            return "MANUAL_REVIEW";
        } else {
            return "DENY";
        }
    }

    /**
     * Creates a default risk assessment for error cases
     */
    private RiskAssessmentResponse createDefaultRiskAssessment(UUID accountId) {
        logger.warn("Creating default risk assessment for account: {}", accountId);

        return RiskAssessmentResponse.builder()
                .riskScore(5.0) // Moderate risk
                .modelVersion(modelVersion)
                .confidenceScore(0.5) // Lower confidence due to error
                .reasonCodes(new String[]{"DEFAULT_ASSESSMENT", "ERROR_FALLBACK"})
                .recommendedAction("REVIEW")
                .assessmentContext("ERROR_FALLBACK")
                .fallbackAssessment(true)
                .build();
    }
}

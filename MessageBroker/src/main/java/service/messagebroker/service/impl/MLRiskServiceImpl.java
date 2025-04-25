package service.messagebroker.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import service.messagebroker.models.RiskAssessmentResponse;
import service.messagebroker.request.RiskAssessmentRequest;
import service.messagebroker.service.MLRiskService;

import javax.naming.ServiceUnavailableException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Service for interacting with the Machine Learning Risk Assessment API
 * Provides risk evaluation for financial transactions using ML models
 */
@Service
public class MLRiskServiceImpl implements MLRiskService {
    private static final Logger logger = LoggerFactory.getLogger(MLRiskServiceImpl.class);
    private RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    @Value("${app.ml.risk-service.url}")
    private String riskServiceUrl;

    @Value("${app.ml.risk-service.timeout:3000}")
    private int serviceTimeout;

    @Value("${app.ml.risk-service.model-version:v2.1.0}")
    private String modelVersion;

    @Value("${app.ml.risk-service.enabled:true}")
    private boolean serviceEnabled;

    public MLRiskServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    /**
     * Evaluates the risk of a transaction using machine learning models
     *
     * @param request The risk assessment request containing transaction details
     * @return A response containing risk scores and evaluation results
     * @throws ServiceUnavailableException If the ML service is unavailable
     */
    @Override
    public RiskAssessmentResponse evaluateRisk(RiskAssessmentRequest request) throws ServiceUnavailableException {
        if (!serviceEnabled) {
            logger.info("ML Risk Service is disabled, using simulated response");
            return createSimulatedResponse(request);
        }

        try {
            logger.debug("Sending risk assessment request to ML service: {}", request.getAccountId());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("X-API-Key", "${app.ml.risk-service.api-key}"); // Would be properly configured in production

            HttpEntity<RiskAssessmentRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<RiskAssessmentResponse> response = restTemplate.exchange(
                    riskServiceUrl,
                    HttpMethod.POST,
                    entity,
                    RiskAssessmentResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("Received risk assessment for account {}: score={}",
                        request.getAccountId(), response.getBody().getRiskScore());
                return response.getBody();
            } else {
                logger.error("ML service returned unsuccessful response: {}", response.getStatusCode());
                throw new ServiceUnavailableException("ML risk service returned unsuccessful response");
            }
        } catch (RestClientException e) {
            logger.error("Error calling ML risk service: {}", e.getMessage());
            throw new ServiceUnavailableException("ML risk service unavailable: " + e.getMessage());
        }
    }

    /**
     * Creates a simulated risk assessment response for testing or when the service is unavailable
     *
     * @param request The original risk assessment request
     * @return A simulated risk assessment response
     */
    public RiskAssessmentResponse createSimulatedResponse(RiskAssessmentRequest request) {
        // Base risk on transaction amount - higher amounts have higher base risk
        double amount = request.getTransactionAmount();
        double baseRisk = Math.min(amount / 2000.0, 5.0);

        // Add risk for international transactions
        double locationRisk = "INTERNATIONAL".equals(request.getLocation()) ? 2.0 : 0.0;

        // Add risk for certain transaction types
        double typeRisk = 0.0;
        if ("WIRE_TRANSFER".equals(request.getTransactionType())) {
            typeRisk = 1.5;
        } else if ("CASH_ADVANCE".equals(request.getTransactionType())) {
            typeRisk = 2.0;
        }

        // Add a small random factor
        double randomFactor = random.nextDouble() * 2.0;

        // Calculate final risk score (0-10 scale)
        double riskScore = Math.min(baseRisk + locationRisk + typeRisk + randomFactor, 10.0);

        // Generate reason codes based on risk factors
        String[] reasonCodes = generateReasonCodes(amount, request.getLocation(), request.getTransactionType());

        // Determine recommended action based on risk score
        String recommendedAction = determineRecommendedAction(riskScore);

        // Calculate confidence score (higher for lower risk scores)
        double confidenceScore = 0.95 - (riskScore / 20.0);

        return RiskAssessmentResponse.builder()
                .riskScore(riskScore)
                .modelVersion(modelVersion)
                .confidenceScore(confidenceScore)
                .reasonCodes(reasonCodes)
                .recommendedAction(recommendedAction)
                .assessmentContext("SIMULATED")
                .fallbackAssessment(true)
                .build();
    }

    /**
     * Generates reason codes explaining risk factors
     */
    public String[] generateReasonCodes(double amount, String location, String transactionType) {
        Map<String, Boolean> reasons = new HashMap<>();

        if (amount > 10000) {
            reasons.put("HIGH_VALUE_TRANSACTION", true);
        } else if (amount > 5000) {
            reasons.put("ELEVATED_TRANSACTION_AMOUNT", true);
        }

        if ("INTERNATIONAL".equals(location)) {
            reasons.put("INTERNATIONAL_TRANSACTION", true);
        } else if ("HIGH_RISK".equals(location)) {
            reasons.put("HIGH_RISK_LOCATION", true);
        }

        if ("WIRE_TRANSFER".equals(transactionType)) {
            reasons.put("WIRE_TRANSFER_RISK", true);
        } else if ("CASH_ADVANCE".equals(transactionType)) {
            reasons.put("CASH_ADVANCE_RISK", true);
        }

        // Always add at least one reason
        if (reasons.isEmpty()) {
            reasons.put("STANDARD_RISK_ASSESSMENT", true);
        }

        return reasons.keySet().toArray(new String[0]);
    }

    /**
     * Determines recommended action based on risk score
     */
    public String determineRecommendedAction(double riskScore) {
        if (riskScore < 3.0) {
            return "APPROVE";
        } else if (riskScore < 7.0) {
            return "REVIEW";
        } else {
            return "DENY";
        }
    }
}

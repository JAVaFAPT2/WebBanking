package service.messagebroker.service;

import org.springframework.stereotype.Service;
import service.messagebroker.models.RiskAssessmentResponse;
import service.messagebroker.request.RiskAssessmentRequest;

import javax.naming.ServiceUnavailableException;


public interface MLRiskService {
    RiskAssessmentResponse evaluateRisk(RiskAssessmentRequest request) throws ServiceUnavailableException;
    RiskAssessmentResponse createSimulatedResponse(RiskAssessmentRequest request);
    String[] generateReasonCodes(double amount, String location, String transactionType);
    String determineRecommendedAction(double riskScore);
}

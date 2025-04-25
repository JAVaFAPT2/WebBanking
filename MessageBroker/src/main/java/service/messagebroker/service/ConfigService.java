package service.messagebroker.service;

import org.springframework.stereotype.Service;
import service.messagebroker.models.RiskThresholds;


public interface ConfigService {
    RiskThresholds getRiskThresholds(String transactionType);
    void updateConfigValue(String key, Object value);

    double getAccountChangeRiskFactor();

    int getVelocityCheckWindowMinutes();
    int getVelocityTransactionThreshold();
    double getVelocityRiskFactor();

    double getNewBeneficiaryRiskFactor();
}

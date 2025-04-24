package service.messagebroker.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerRiskProfile {
    private String userId;
    private RiskLever riskLevel; // LOW, MEDIUM, HIGH
    private double riskMultiplier;
    private boolean isRestricted;
    private long lastUpdated;
    private String riskReason;
}

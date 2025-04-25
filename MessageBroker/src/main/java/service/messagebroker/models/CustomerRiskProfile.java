package service.messagebroker.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "customer_risk_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRiskProfile {
    @Id
    private UUID userId = UUID.randomUUID();
    private RiskLever riskLevel; // LOW, MEDIUM, HIGH
    private double riskMultiplier;
    private boolean isRestricted;
    private long lastUpdated;
    private String riskReason;
}

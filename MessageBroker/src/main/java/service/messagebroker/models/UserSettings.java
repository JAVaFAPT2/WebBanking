package service.messagebroker.models;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * User settings model class
 */
@Setter
@Getter
public class UserSettings {
    // Getters and setters
    private UUID userId;
    private Double spendingThreshold;
    private Double riskFactor;
    private long lastFetched;

}
package service.messagebroker.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import service.messagebroker.models.RiskThresholds;
import service.messagebroker.service.ConfigService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing configuration settings for the message broker
 * Provides access to risk thresholds, factors, and other configurable parameters
 */
@Service
public class ConfigServiceImpl implements ConfigService {

    // Cache for risk thresholds by transaction type
    private final Map<String, RiskThresholds> thresholdsCache = new ConcurrentHashMap<>();

    /**
     * -- GETTER --
     *  Gets the risk factor for account changes
     *
     */
    // Default values from configuration
    @Value("${app.risk.account-change-factor:1.5}")
    private double accountChangeRiskFactor;
    @Override
    public double getAccountChangeRiskFactor() {
        return accountChangeRiskFactor;
    }

    /**
     * -- GETTER --
     *  Gets the risk factor for transaction velocity
     *
     */

    @Value("${app.risk.velocity-factor:2.0}")
    private double velocityRiskFactor;
    @Override
    public double getVelocityRiskFactor() {
        return velocityRiskFactor;
    }

    /**
     * -- GETTER --
     *  Gets the risk factor for new beneficiaries
     *
     */

    @Value("${app.risk.new-beneficiary-factor:1.8}")
    private double newBeneficiaryRiskFactor;
    @Override
    public double getNewBeneficiaryRiskFactor() {
        return newBeneficiaryRiskFactor;
    }
    /**
     * -- GETTER --
     *  Gets the time window for velocity checks in minutes
     *
     */

    @Value("${app.risk.velocity-check-window:15}")
    private int velocityCheckWindowMinutes;
    @Override
    public int getVelocityCheckWindowMinutes() {
    return velocityCheckWindowMinutes;
    }
    /**
     * -- GETTER --
     *  Gets the threshold for number of transactions in the velocity window
     *
     */

    @Value("${app.risk.velocity-transaction-threshold:3}")
    private int velocityTransactionThreshold;
    @Override
    public int getVelocityTransactionThreshold() {
        return velocityTransactionThreshold;
    }

    /**
     * Gets risk thresholds for a specific transaction type
     *
     * @param transactionType The type of transaction
     * @return Risk thresholds appropriate for the transaction type
     */
    public RiskThresholds getRiskThresholds(String transactionType) {
        // Return from cache if available
        if (thresholdsCache.containsKey(transactionType)) {
            return thresholdsCache.get(transactionType);
        }

        // Create appropriate thresholds based on transaction type
        RiskThresholds thresholds = switch (transactionType) {
            case "WIRE_TRANSFER", "INTERNATIONAL", "CASH_ADVANCE", "CRYPTOCURRENCY" ->
                    RiskThresholds.getHighRiskThresholds();
            case "INTERNAL_TRANSFER", "BILL_PAYMENT", "SCHEDULED_PAYMENT" -> RiskThresholds.getLowRiskThresholds();
            default -> RiskThresholds.getDefaultThresholds();
        };

        // Cache the thresholds
        thresholdsCache.put(transactionType, thresholds);

        return thresholds;
    }

    /**
     * Refreshes the thresholds cache
     * Used when configuration is updated
     */
    public void refreshThresholdsCache() {
        thresholdsCache.clear();
    }

    /**
     * Updates a configuration value at runtime
     *
     * @param key The configuration key
     * @param value The new value
     */
    public void updateConfigValue(String key, Object value) {
        // In a real implementation, this would update the configuration
        // and possibly persist the change to a database or configuration service

        if ("accountChangeRiskFactor".equals(key) && value instanceof Number) {
            accountChangeRiskFactor = ((Number) value).doubleValue();
        } else if ("velocityRiskFactor".equals(key) && value instanceof Number) {
            velocityRiskFactor = ((Number) value).doubleValue();
        } else if ("newBeneficiaryRiskFactor".equals(key) && value instanceof Number) {
            newBeneficiaryRiskFactor = ((Number) value).doubleValue();
        } else if ("velocityCheckWindowMinutes".equals(key) && value instanceof Number) {
            velocityCheckWindowMinutes = ((Number) value).intValue();
        } else if ("velocityTransactionThreshold".equals(key) && value instanceof Number) {
            velocityTransactionThreshold = ((Number) value).intValue();
        }

        // Clear cache after config update
        refreshThresholdsCache();
    }
}

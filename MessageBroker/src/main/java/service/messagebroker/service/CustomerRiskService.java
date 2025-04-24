package service.messagebroker.service;

import service.messagebroker.models.CustomerRiskProfile;
import java.util.UUID;

/**
 * Service for retrieving and managing customer risk profiles
 */
public interface CustomerRiskService {
    /**
     * Get a customer's risk profile
     * @param userId The customer's user ID
     * @return The customer's risk profile
     */
    CustomerRiskProfile getCustomerRiskProfile(String userId);

    /**
     * Update a customer's risk profile
     * @param profile The updated risk profile
     * @return The updated profile
     */
    CustomerRiskProfile updateCustomerRiskProfile(CustomerRiskProfile profile);
}

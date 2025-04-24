package service.messagebroker.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.messagebroker.models.CustomerRiskProfile;
import service.messagebroker.models.RiskLever;
import service.messagebroker.service.CustomerRiskService;
import service.repository.CustomerRiskProfileRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of CustomerRiskService
 */
@Service
public class CustomerRiskServiceImpl implements CustomerRiskService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerRiskServiceImpl.class);

    // Cache for frequently accessed profiles
    private final Map<String, CustomerRiskProfile> profileCache = new HashMap<>();

    @Autowired
    private CustomerRiskProfileRepository repository;

    @Override
    public CustomerRiskProfile getCustomerRiskProfile(String userId) {
        try {
            // Check cache first
            if (profileCache.containsKey(userId)) {
                return profileCache.get(userId);
            }

            // Try to get from repository
            CustomerRiskProfile profile = repository.findByUserId(userId)
                    .orElseGet(() -> createDefaultProfile(userId));

            // Cache the result
            profileCache.put(userId, profile);

            return profile;
        } catch (Exception e) {
            logger.error("Error retrieving customer risk profile: {}", e.getMessage());
            return createDefaultProfile(userId);
        }
    }

    @Override
    public CustomerRiskProfile updateCustomerRiskProfile(CustomerRiskProfile profile) {
        try {
            // Update in repository
            CustomerRiskProfile savedProfile = repository.save(profile);

            // Update cache
            profileCache.put(profile.getUserId(), savedProfile);

            return savedProfile;
        } catch (Exception e) {
            logger.error("Error updating customer risk profile: {}", e.getMessage());
            return profile;
        }
    }

    private CustomerRiskProfile createDefaultProfile(String userId) {
        CustomerRiskProfile profile = new CustomerRiskProfile();
        profile.setUserId(userId);
        profile.setRiskLevel(RiskLever.MEDIUM);
        profile.setRiskMultiplier(1.0);
        profile.setLastUpdated(System.currentTimeMillis());
        return profile;
    }
}

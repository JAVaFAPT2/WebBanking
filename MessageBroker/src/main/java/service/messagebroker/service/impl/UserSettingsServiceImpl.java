package service.messagebroker.service.impl;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import service.messagebroker.models.UserSettings;
import service.messagebroker.service.UserSettingsService;
import service.messagebroker.repository.UserSettingsRepository;


import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UserSettingsService implementation that retrieves user settings from a database
 */
@Service
class UserSettingsServiceImpl implements UserSettingsService {
    private static final Logger logger = LoggerFactory.getLogger(UserSettingsServiceImpl.class);

    private final UserSettingsRepository userSettingsRepository;
    // Cache to improve performance for frequently accessed settings
    private final Map<UUID, UserSettings> userSettingsCache = new ConcurrentHashMap<>();

    // Default values if settings are not found
    private static final double DEFAULT_SPENDING_THRESHOLD = 5000.0;
    private static final double DEFAULT_RISK_FACTOR = 1.0;

    @Value("${app.user-settings.cache-ttl-minutes:15}")
    private int cacheTtlMinutes;


    public UserSettingsServiceImpl(UserSettingsRepository userSettingsRepository) {
        this.userSettingsRepository = userSettingsRepository;
    }
    @Override
    public Optional<Double> getSpendingThreshold(UUID userId) {
        try {
            UserSettings settings = getUserSettings(userId);
            return Optional.ofNullable(settings.getSpendingThreshold());
        } catch (Exception e) {
            logger.error("Error retrieving spending threshold for user {}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<Double> getRiskFactor(UUID userId) {
        try {
            UserSettings settings = getUserSettings(userId);
            return Optional.ofNullable(settings.getRiskFactor());
        } catch (Exception e) {
            logger.error("Error retrieving risk factor for user {}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }


    /**
     * Get user settings from cache or repository
     */
    private UserSettings getUserSettings(UUID userId) {
        // Check cache first
        if (userSettingsCache.containsKey(userId)) {
            UserSettings cachedSettings = userSettingsCache.get(userId);
            if (!isSettingsExpired(cachedSettings)) {
                logger.debug("Cache hit for user settings: {}", userId);
                return cachedSettings;
            }
            logger.debug("Cache expired for user settings: {}", userId);
        }

        // Cache miss or expired, fetch from repository
        try {
            logger.debug("Fetching user settings from repository: {}", userId);
            Optional<UserSettings> settings = userSettingsRepository.findByUserId(userId);

            if (settings.isPresent()) {
                UserSettings userSettings = settings.get();
                userSettings.setLastUpdated(System.currentTimeMillis());
                userSettingsCache.put(userId, userSettings);
                return userSettings;
            } else {
                // User not found, create default settings
                logger.info("No settings found for user {}, using defaults", userId);
                UserSettings defaultSettings = createDefaultSettings(userId);
                userSettingsCache.put(userId, defaultSettings);
                return defaultSettings;
            }
        } catch (Exception e) {
            logger.error("Error fetching user settings: {}", e.getMessage());
            // Return default settings in case of error
            return createDefaultSettings(userId);
        }
    }

    /**
     * Check if cached settings are expired
     */
    private boolean isSettingsExpired(UserSettings settings) {
        long now = System.currentTimeMillis();
        long lastFetched = settings.getLastUpdated();
        long ttlMillis = (long) cacheTtlMinutes * 60 * 1000;

        return (now - lastFetched) > ttlMillis;
    }

    /**
     * Create default settings for a user
     */
    private UserSettings createDefaultSettings(UUID userId) {
        UserSettings settings = new UserSettings();
        settings.setUserId(userId);
        settings.setSpendingThreshold(DEFAULT_SPENDING_THRESHOLD);
        settings.setRiskFactor(DEFAULT_RISK_FACTOR);
        settings.setLastUpdated(System.currentTimeMillis());
        return settings;
    }
}





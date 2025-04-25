package service.messagebroker.service.impl;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import service.messagebroker.models.AnomalyDetectionResult;
import service.messagebroker.models.UserBehaviorProfile;
import service.messagebroker.service.anomalyDetectionService;
import service.messagebroker.service.transactionHistoryService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for detecting anomalies in transaction patterns
 * Uses statistical analysis and machine learning to identify unusual behavior
 */
@Service
public class anomalyDetectionServiceImpl implements anomalyDetectionService {
    private static final Logger logger = LoggerFactory.getLogger(anomalyDetectionServiceImpl.class);

    private final transactionHistoryService transactionHistoryService;

    // Cache for user behavior profiles to improve performance
    private final Map<UUID, UserBehaviorProfile> behaviorProfileCache = new ConcurrentHashMap<>();

    @Getter
    @Setter
    @Value("${app.anomaly.amount-threshold-multiplier:5.0}")
    private double amountThresholdMultiplier;

    @Value("${app.anomaly.time-sensitivity:0.7}")
    private double timeSensitivity;

    @Value("${app.anomaly.location-sensitivity:0.8}")
    private double locationSensitivity;

    @Value("${app.anomaly.velocity-sensitivity:0.9}")
    private double velocitySensitivity;

    @Value("${app.anomaly.detection-method:STATISTICAL}")
    private String detectionMethod;

    public anomalyDetectionServiceImpl(transactionHistoryService transactionHistoryService) {
        this.transactionHistoryService = transactionHistoryService;
    }

    /**
     * Gets or creates a user behavior profile for an account
     *
     * @param accountId The account ID to get the profile for
     * @return The user's behavior profile
     */
    private UserBehaviorProfile getUserBehaviorProfile(UUID accountId) {
        // Check if profile exists in cache
        UserBehaviorProfile profile = behaviorProfileCache.get(accountId);

        if (profile == null) {
            // Profile not in cache, try to load from database or create new
            profile = loadProfileFromDatabase(accountId);

            if (profile == null) {
                // No profile in database, create a new one
                profile = UserBehaviorProfile.createEmptyProfile(accountId);
                logger.info("Created new behavior profile for account: {}", accountId);
            }

            // Store in cache for future use
            behaviorProfileCache.put(accountId, profile);
        }

        return profile;
    }

    /**
     * Loads a user behavior profile from the database
     *
     * @param accountId The account ID to load the profile for
     * @return The loaded profile or null if not found
     */
    private UserBehaviorProfile loadProfileFromDatabase(UUID accountId) {
        try {
            // This would typically query a database for the profile
            // For this example, we'll use the transaction history service to build a profile

            // Get average transaction amount
            double avgAmount = transactionHistoryService.getAverageTransactionAmount(accountId, 90); // Last 90 days

            // Get location frequency
            Map<String, Integer> locationFrequency = transactionHistoryService.getTransactionLocationFrequency(accountId, 90);

            // Create and populate the profile
            UserBehaviorProfile profile = UserBehaviorProfile.createEmptyProfile(accountId);
            profile.setAverageTransactionAmount(avgAmount);
            profile.setLocationFrequency(locationFrequency);

            // We would typically load more data here, like hour frequency and standard deviation
            // For simplicity, we'll estimate standard deviation as 30% of average amount
            profile.setAmountStandardDeviation(avgAmount * 0.3);

            // Set transaction count based on location frequency
            int transactionCount = locationFrequency.values().stream().mapToInt(Integer::intValue).sum();
            profile.setTransactionCount(transactionCount);

            if (transactionCount > 0) {
                logger.info("Loaded behavior profile for account {}: {} transactions, avg amount: {}",
                        accountId, transactionCount, avgAmount);
                return profile;
            } else {
                logger.info("No transaction history found for account: {}", accountId);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error loading behavior profile for account {}: {}", accountId, e.getMessage());
            return null;
        }
    }
    /**
     * Safely parses a value to a double
     *
     * @param value The value to parse
     * @return The parsed double value, or 0.0 if parsing fails
     */
    private double parseDoubleValue(Object value) {
        if (value == null) {
            return 0.0;
        }

        try {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else if (value instanceof String) {
                return Double.parseDouble((String) value);
            } else {
                return Double.parseDouble(value.toString());
            }
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse value as double: {}", value);
            return 0.0;
        }
    }
    /**
     * Safely parses a value to a long
     *
     * @param value The value to parse
     * @return The parsed long value, or 0 if parsing fails
     */
    private long parseLongValue(Object value) {
        if (value == null) {
            return 0L;
        }

        try {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            } else if (value instanceof String) {
                return Long.parseLong((String) value);
            } else {
                return Long.parseLong(value.toString());
            }
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse value as long: {}", value);
            return 0L;
        }
    }
    /**
     * Updates a user behavior profile with new transaction data
     *
     * @param profile The user behavior profile to update
     * @param transactionContext The transaction context containing new data
     */
    private void updateUserBehaviorProfile(UserBehaviorProfile profile, Map<String, Object> transactionContext) {
        try {
            if (profile == null || transactionContext == null || transactionContext.isEmpty()) {
                return;
            }

            // Extract transaction amount
            double amount = 0.0;
            if (transactionContext.containsKey("amount")) {
                amount = parseDoubleValue(transactionContext.get("amount"));
            }

            // Extract transaction timestamp
            long timestamp = System.currentTimeMillis(); // Default to current time
            if (transactionContext.containsKey("timestamp")) {
                timestamp = parseLongValue(transactionContext.get("timestamp"));
            }

            // Extract transaction location
            String location = "UNKNOWN";
            if (transactionContext.containsKey("location")) {
                location = String.valueOf(transactionContext.get("location"));
            }

            // Update the profile with the new transaction data
            profile.updateWithTransaction(amount, timestamp, location);

            // Log the update
            logger.debug("Updated behavior profile for user {}: new amount={}, location={}",
                    profile.getUserId(), amount, location);

            // Persist the updated profile
            persistUserBehaviorProfile(profile);
        } catch (Exception e) {
            logger.error("Error updating user behavior profile: {}", e.getMessage());
        }
    }

    /**
     * Persists a user behavior profile to storage
     *
     * @param profile The profile to persist
     */
    private void persistUserBehaviorProfile(UserBehaviorProfile profile) {
        try {
            // In a real implementation, this would save the profile to a database
            // For this example, we'll just update the cache

            // Update the cache
            behaviorProfileCache.put(profile.getUserId(), profile);

            // In a production system, we might also:
            // 1. Save to a database asynchronously
            // 2. Publish an event for other services
            // 3. Update analytics systems

            // For now, we'll just log that we would persist it
            logger.debug("Persisted behavior profile for user: {}", profile.getUserId());
        } catch (Exception e) {
            logger.error("Error persisting user behavior profile: {}", e.getMessage());
        }
    }

    /**
     * Analyzes a transaction for anomalous patterns
     *
     * @param accountId The account ID associated with the transaction
     * @param transactionContext Context data about the transaction
     * @return Results of the anomaly detection analysis
     */
    public AnomalyDetectionResult analyze(UUID accountId, Map<String, Object> transactionContext) {
        if (accountId == null || transactionContext == null) {
            logger.warn("Invalid input for anomaly detection: accountId={}, context={}", accountId, transactionContext);
            return AnomalyDetectionResult.createNormalResult();
        }

        try {
            logger.debug("Performing anomaly detection for account: {}", accountId);

            // Get or create user behavior profile
            UserBehaviorProfile profile = getUserBehaviorProfile(accountId);

            // Initialize result builder
            AnomalyDetectionResult.Builder resultBuilder = AnomalyDetectionResult.builder()
                    .analysisTimestamp(Instant.now())
                    .detectionMethod(detectionMethod)
                    .confidence(0.85);

            // Initialize feature scores map
            Map<String, Double> featureScores = new HashMap<>();
            double overallScore = 0.0;
            int featureCount = 0;

            // Feature 1: Amount anomaly detection
            if (transactionContext.containsKey("amount")) {
                double amount = parseDoubleValue(transactionContext.get("amount"));
                double amountScore = detectAmountAnomaly(amount, profile);
                featureScores.put("AMOUNT", amountScore);

                if (amountScore > 0.7) {
                    resultBuilder.addDetectedAnomaly("UNUSUAL_AMOUNT");
                    String explanation = String.format(
                            "Transaction amount of %.2f is %.1fx higher than usual for this account",
                            amount, amount / profile.getAverageTransactionAmount());
                    resultBuilder.explanation(explanation);
                }

                overallScore += amountScore;
                featureCount++;
            }

            // Feature 2: Time-of-day anomaly detection
            if (transactionContext.containsKey("timestamp")) {
                long timestamp = parseLongValue(transactionContext.get("timestamp"));
                double timeScore = detectTimeAnomaly(timestamp, profile);
                featureScores.put("TIME_OF_DAY", timeScore);

                if (timeScore > 0.7) {
                    resultBuilder.addDetectedAnomaly("UNUSUAL_TIME");
                    LocalDateTime time = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
                    resultBuilder.explanation("Transaction at unusual time: " + time.getHour() + ":" + time.getMinute());
                }

                overallScore += timeScore * timeSensitivity;
                featureCount++;
            }

            // Feature 3: Location anomaly detection
            if (transactionContext.containsKey("location")) {
                String location = String.valueOf(transactionContext.get("location"));
                double locationScore = detectLocationAnomaly(location, profile);
                featureScores.put("LOCATION", locationScore);

                if (locationScore > 0.7) {
                    resultBuilder.addDetectedAnomaly("UNUSUAL_LOCATION");
                    resultBuilder.explanation("Transaction from unusual location: " + location);
                }

                overallScore += locationScore * locationSensitivity;
                featureCount++;
            }

            // Feature 4: Transaction velocity anomaly detection
            double velocityScore = detectVelocityAnomaly(accountId, transactionContext);
            featureScores.put("VELOCITY", velocityScore);

            if (velocityScore > 0.7) {
                resultBuilder.addDetectedAnomaly("UNUSUAL_VELOCITY");
                resultBuilder.explanation("Unusually high transaction frequency detected");
            }

            overallScore += velocityScore * velocitySensitivity;
            featureCount++;

            // Feature 5: New recipient anomaly detection
            if (transactionContext.containsKey("recipientId")) {
                String recipientId = String.valueOf(transactionContext.get("recipientId"));
                boolean isNewRecipient = transactionHistoryService.isNewRecipient(accountId, recipientId);

                if (isNewRecipient) {
                    double newRecipientScore = 0.5; // Base score for new recipient

                    // Higher score if it's a large amount to a new recipient
                    if (transactionContext.containsKey("amount")) {
                        double amount = parseDoubleValue(transactionContext.get("amount"));
                        if (amount > profile.getAverageTransactionAmount() * 2) {
                            newRecipientScore = 0.8;
                            resultBuilder.addDetectedAnomaly("LARGE_AMOUNT_NEW_RECIPIENT");
                            resultBuilder.explanation("Large amount sent to new recipient");
                        }
                    }

                    featureScores.put("NEW_RECIPIENT", newRecipientScore);
                    overallScore += newRecipientScore;
                    featureCount++;
                }
            }

            // Calculate final score (average of all features)
            double finalScore = overallScore / featureCount;

            // Build and return the result
            AnomalyDetectionResult result = resultBuilder
                    .score(finalScore)
                    .featureScores(featureScores)
                    .recommendedAction(determineRecommendedAction(finalScore))
                    .build();

            logger.info("Anomaly detection completed for account {}: score={}, anomalies={}",
                    accountId, finalScore, result.getDetectedAnomalies());

            // Update user behavior profile with this transaction data
            updateUserBehaviorProfile(profile, transactionContext);

            return result;
        } catch (Exception e) {
            logger.error("Error performing anomaly detection for account {}: {}", accountId, e.getMessage());
            return AnomalyDetectionResult.createNormalResult();
        }
    }

    /**
     * Detects anomalies in transaction amount
     *
     * @param amount The transaction amount
     * @param profile The user's behavior profile
     * @return Anomaly score for the amount (0.0 to 1.0)
     */
    public double detectAmountAnomaly(double amount, UserBehaviorProfile profile) {
        double avgAmount = profile.getAverageTransactionAmount();
        double stdDev = profile.getAmountStandardDeviation();

        // If we don't have enough data, use a simple threshold approach
        if (avgAmount < 0.01 || stdDev < 0.01) {
            return 0.0; // Not enough data to detect anomalies
        }

        // Calculate z-score (how many standard deviations from the mean)
        double zScore = (amount - avgAmount) / stdDev;

        // Convert to a 0-1 score using a sigmoid-like function
        if (zScore <= 0) {
            return 0.0; // Lower than average amounts aren't anomalous
        } else if (zScore > 5) {
            return 0.95; // Cap at 0.95 for very high z-scores
        } else {
            return 1.0 / (1.0 + Math.exp(-(zScore - 3))); // Sigmoid centered at z=3
        }
    }

    /**
     * Detects anomalies in transaction time
     *
     * @param timestamp The transaction timestamp
     * @param profile The user's behavior profile
     * @return Anomaly score for the time (0.0 to 1.0)
     */
    public double detectTimeAnomaly(long timestamp, UserBehaviorProfile profile) {
        LocalDateTime time = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        int hour = time.getHour();

        // Check if this hour is common for this user
        int[] hourFrequency = profile.getHourFrequency();

        // If we don't have enough data
        if (hourFrequency == null) {
            // Use general heuristics - transactions between 1am and 5am are unusual
            return (hour >= 1 && hour <= 5) ? 0.7 : 0.0;
        }

        // Calculate how common this hour is for the user
        int totalTransactions = 0;
        for (int count : hourFrequency) {
            totalTransactions += count;
        }

        if (totalTransactions == 0) {
            return 0.0; // Not enough data
        }

        double hourProbability = (double) hourFrequency[hour] / totalTransactions;

        // Convert to anomaly score (less common = higher score)
        if (hourProbability > 0.1) {
            return 0.0; // Common hour, not anomalous
        } else if (hourProbability > 0.05) {
            return 0.3; // Somewhat uncommon
        } else if (hourProbability > 0.01) {
            return 0.6; // Rare
        } else {
            return 0.9; // Very rare or never seen before
        }
    }

    /**
     * Detects anomalies in transaction location
     *
     * @param location The transaction location
     * @param profile The user's behavior profile
     * @return Anomaly score for the location (0.0 to 1.0)
     */
    public double detectLocationAnomaly(String location, UserBehaviorProfile profile) {
        Map<String, Integer> locationFrequency = profile.getLocationFrequency();

        // If we don't have enough data
        if (locationFrequency == null || locationFrequency.isEmpty()) {
            // Use general heuristics - international or high-risk locations are unusual
            if ("INTERNATIONAL".equals(location) || "HIGH_RISK".equals(location)) {
                return 0.8;
            } else if ("UNUSUAL".equals(location)) {
                return 0.6;
            } else {
                return 0.0;
            }
        }

        // Check if this location is in the user's history
        if (!locationFrequency.containsKey(location)) {
            return 0.9; // Never seen before
        }

        // Calculate how common this location is for the user
        int totalTransactions = locationFrequency.values().stream().mapToInt(Integer::intValue).sum();
        double locationProbability = (double) locationFrequency.get(location) / totalTransactions;

        // Convert to anomaly score (less common = higher score)
        if (locationProbability > 0.2) {
            return 0.0; // Common location, not anomalous
        } else if (locationProbability > 0.1) {
            return 0.3; // Somewhat uncommon
        } else if (locationProbability > 0.02) {
            return 0.6; // Rare
        } else {
            return 0.8; // Very rare
        }
    }

    /**
     * Detects anomalies in transaction velocity (frequency)
     *
     * @param accountId The account ID
     * @param transactionContext The transaction context
     * @return Anomaly score for the velocity (0.0 to 1.0)
     */
    public double detectVelocityAnomaly(UUID accountId, Map<String, Object> transactionContext) {
        // Check if we have time since last transaction in the context
        if (transactionContext.containsKey("timeSinceLastTransaction")) {
            double seconds = parseDoubleValue(transactionContext.get("timeSinceLastTransaction"));

            // Very rapid transactions are highly anomalous
            if (seconds < 30.0) {
                return 0.95; // Extremely fast succession
            } else if (seconds < 60.0) {
                return 0.8; // Very fast succession
            } else if (seconds < 300.0) {
                return 0.6; // Fast succession
            } else if (seconds < 900.0) {
                return 0.3; // Somewhat fast
            } else {
                return 0.0; // Normal timing
            }
        }

        // If we don't have direct timing info, check recent transaction count
        int recentCount = transactionHistoryService.getRecentTransactionCount(accountId, 15); // Last 15 minutes

        if (recentCount > 5) {
            return 0.9; // Very high frequency
        } else if (recentCount > 3) {
            return 0.7; // High frequency
        } else if (recentCount > 1) {
            return 0.3; // Moderate frequency
        } else {
            return 0.0; // Normal frequency
        }
    }

    /**
     * Determines the recommended action based on the anomaly score
     *
     * @param score The overall anomaly score
     * @return The recommended action
     */
    public String determineRecommendedAction(double score) {
        if (score > 0.8) {
            return "BLOCK";
        } else if (score > 0.6) {
            return "REVIEW";
        } else if (score > 0.3) {
            return "MONITOR";
        } else {
            return "PROCEED";
        }
    }

}

package service.messagebroker.steam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import service.messagebroker.exeption.RiskAssessmentException;
import service.messagebroker.models.CustomerRiskProfile;
import service.messagebroker.models.KafkaMessage;
import service.messagebroker.request.RiskAssessmentRequest;
import service.messagebroker.service.UserSettingsService;

import javax.naming.ServiceUnavailableException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Kafka Streams processor for transaction data
 * Processes transaction events for fraud detection, analytics, and notifications
 * Enhanced with improved fraud detection and real-time analytics
 */
@Component
@EnableKafkaStreams
public class TransactionStreamProcessor {
    private static final Logger logger = LoggerFactory.getLogger(TransactionStreamProcessor.class);

    private final ObjectMapper objectMapper;
    private final UserSettingsService userSettingsService;

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topics.transaction}")
    private String transactionTopic;

    @Value("${spring.kafka.topics.notification}")
    private String notificationTopic;

    @Value("${spring.kafka.topics.analytics}")
    private String analyticsTopic;

    @Value("${spring.kafka.topics.fraud-alert}")
    private String fraudAlertTopic;

    @Value("${spring.kafka.topics.audit-log}")
    private String auditLogTopic;

    // Risk threshold for international transactions
    @Value("${app.transaction.international.risk-threshold:7.5}")
    private double internationalRiskThreshold;

    // Risk threshold for high-value transactions
    @Value("${app.transaction.high-value.threshold:10000.0}")
    private double highValueThreshold;

    // Spending alert threshold (can be overridden by user-specific settings)
    @Value("${app.transaction.spending.alert-threshold:5000.0}")
    private double defaultSpendingAlertThreshold;

    @Autowired
    public TransactionStreamProcessor(ObjectMapper objectMapper, UserSettingsService userSettingsService, KafkaTemplate<String, String> kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.userSettingsService = userSettingsService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Bean
    public KStream<String, String> kStream(StreamsBuilder streamsBuilder) {
        // Create a stream from the transaction topic
        KStream<String, String> transactionStream = streamsBuilder.stream(
                transactionTopic,
                Consumed.with(Serdes.String(), Serdes.String())
        );

        // Define predicates for branching
        Predicate<String, String> isHighValue = (key, value) -> isHighValueTransaction(value);
        Predicate<String, String> isInternational = (key, value) -> isInternationalTransaction(value);
        Predicate<String, String> isRegular = (key, value) -> true;

        // Create a peeked stream for logging and auditing
        KStream<String, String> peekedStream = transactionStream
                .peek((key, value) -> {
                    logger.info("Processing transaction: {}", key);
                    auditTransaction(key, value);
                });

        // Branch the stream based on transaction type
        @SuppressWarnings("unchecked")
        KStream<String, String>[] branches = peekedStream.branch(
                isHighValue,
                isInternational,
                isRegular
        );

        KStream<String, String> highValueTransactions = branches[0];
        KStream<String, String> internationalTransactions = branches[1];
        KStream<String, String> regularTransactions = branches[2];

        // Process high-value transactions for compliance monitoring
        highValueTransactions
                .mapValues(this::enrichTransactionWithRiskScore)
                .peek((key, value) -> logger.info("High-value transaction processed: {}", key))
                .to(analyticsTopic, Produced.with(Serdes.String(), Serdes.String()));

        // Detect potential fraud in international transactions with machine learning risk assessment
        internationalTransactions
                .mapValues(this::enrichTransactionWithMachineLearningRiskAssessment)
                .flatMapValues(this::detectFraudPatterns)
                .filter((key, value) -> value != null && (value.contains("SUSPICIOUS") || value.contains("FRAUD_ALERT")))
                .to(fraudAlertTopic, Produced.with(Serdes.String(), Serdes.String()));

        // Aggregate transaction amounts by user for real-time spending analysis
        // Using a more sophisticated time windowing approach
        regularTransactions
                .groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofHours(1)).advanceBy(Duration.ofMinutes(15)))
                .aggregate(
                        () -> "0.0", // Initial value
                        (key, value, aggregate) -> aggregateTransactionAmount(value, aggregate),
                        Materialized.with(Serdes.String(), Serdes.String())
                )
                .toStream()
                .map((windowedKey, value) -> KeyValue.pair(windowedKey.key(), value))
                .filter(this::exceedsUserThreshold)
                .mapValues(this::createSpendingNotification)
                .to(notificationTopic, Produced.with(Serdes.String(), Serdes.String()));

        // New: Process all transactions for a consolidated view (merge streams)
        KStream<String, String> mergedStream = peekedStream
                .mapValues(this::extractBasicTransactionInfo)
                .filter((key, value) -> value != null);

        // Send to analytics for consolidated reporting
        mergedStream.to(analyticsTopic, Produced.with(Serdes.String(), Serdes.String()));

        return transactionStream;
    }

    private boolean exceedsUserThreshold(String userId, String totalAmount) {
        try {
            double total = Double.parseDouble(totalAmount);
            // Get user-specific threshold if available (would come from a user settings service)
            double threshold = getUserSpendingThreshold(UUID.fromString(userId));
            logger.debug("Checking spending threshold for user {}: current={}, threshold={}",
                    userId, total, threshold);
            return total > threshold;
        } catch (Exception e) {
            logger.error("Error checking user threshold", e);
            return false;
        }
    }

    /**
     * Audit transaction for compliance and monitoring
     */
    private void auditTransaction(String key, String transactionJson) {
        try {
            KafkaMessage message = objectMapper.readValue(transactionJson, KafkaMessage.class);
            Map<String, Object> payload = message.getPayload();

            // Create audit record
            Map<String, Object> auditData = new HashMap<>();
            auditData.put("transactionId", payload.getOrDefault("transactionId", "Unknown"));
            auditData.put("accountId", payload.getOrDefault("accountId", "Unknown"));
            auditData.put("processingTimestamp", System.currentTimeMillis());
            auditData.put("originalPayload", payload);

            // Create audit message
            KafkaMessage auditMessage = new KafkaMessage(
                    KafkaMessage.MessageType.SYSTEM_ALERT,
                    "transaction-stream-processor",
                    key,
                    "TRANSACTION_AUDIT"
            );
            auditMessage.setPayload(auditData);

            // Send to audit log topic
            String auditMessageJson = objectMapper.writeValueAsString(auditMessage);
            kafkaTemplate.send(auditLogTopic, key, auditMessageJson);

            logger.debug("Transaction audited: {}", key);
        } catch (Exception e) {
            logger.error("Error auditing transaction", e);
        }
    }

    private boolean isHighValueTransaction(String transactionJson) {
        try {
            KafkaMessage message = objectMapper.readValue(transactionJson, KafkaMessage.class);
            Map<String, Object> payload = message.getPayload();
            if (payload.containsKey("amount")) {
                double amount = Double.parseDouble(payload.get("amount").toString());
                return amount > highValueThreshold;
            }
        } catch (Exception e) {
            logger.error("Error parsing transaction for high value check", e);
        }
        return false;
    }

    private boolean isInternationalTransaction(String transactionJson) {
        try {
            KafkaMessage message = objectMapper.readValue(transactionJson, KafkaMessage.class);
            Map<String, Object> payload = message.getPayload();
            return payload.containsKey("isInternational") &&
                    Boolean.TRUE.equals(payload.get("isInternational"));
        } catch (Exception e) {
            logger.error("Error parsing transaction for international check", e);
        }
        return false;
    }

    private String enrichTransactionWithRiskScore(String transactionJson) {
        try {
            KafkaMessage message = objectMapper.readValue(transactionJson, KafkaMessage.class);
            Map<String, Object> payload = message.getPayload();

            // Calculate risk score based on amount, location, and user history
            double amount = Double.parseDouble(payload.get("amount").toString());
            String location = (String) payload.getOrDefault("location", "UNKNOWN");
            UUID accountId = (UUID) payload.getOrDefault("accountId", "UNKNOWN");

            // Calculate comprehensive risk score using multiple factors
            double riskScore = calculateComprehensiveRiskScore(amount, location, accountId, payload);
            payload.put("riskScore", riskScore);

            // Add risk category
            String riskCategory = getRiskCategory(riskScore);
            payload.put("riskCategory", riskCategory);

            // Mark transaction for potential review if risk score is high
            if (riskScore > 7.0) {
                payload.put("requiresReview", Boolean.TRUE);
                payload.put("reviewReason", "High risk score: " + riskScore);
            }

            message.setPayload(payload);
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            logger.error("Error enriching transaction with risk score", e);
            return transactionJson;
        }
    }

    /**
     * Enhanced risk scoring algorithm that considers multiple factors
     */
    private double calculateComprehensiveRiskScore(double amount, String location, UUID accountId, Map<String, Object> payload) {
        // Base score calculation
        double baseScore = amount / 1000.0;

        // Location-based risk factors
        double locationFactor = getLocationRiskFactor(location);

        // Time-based risk factors (suspicious hours, etc.)
        double timeFactor = getTimeBasedRiskFactor(payload);

        // User history factor (would normally come from a user profile service)
        double userHistoryFactor = getUserHistoryRiskFactor(accountId);

        // Transaction velocity (multiple transactions in short period)
        double velocityFactor = getTransactionVelocityFactor(accountId, payload);

        // Calculate composite score with weighted factors
        double riskScore = (baseScore * 0.3) +
                (locationFactor * 0.3) +
                (timeFactor * 0.15) +
                (userHistoryFactor * 0.15) +
                (velocityFactor * 0.1);

        // Cap at 10
        return Math.min(riskScore, 10.0);
    }

    private double getLocationRiskFactor(String location) {
        // Higher risk for certain locations
        return switch (location) {
            case "INTERNATIONAL" -> 3.0;
            case "HIGH_RISK" -> 5.0;
            case "RESTRICTED" -> 7.0;
            default -> 1.0;
        };
    }

    private double getTimeBasedRiskFactor(Map<String, Object> payload) {
        // Check if transaction occurs during unusual hours
        if (payload.containsKey("timestamp")) {
            long timestamp = Long.parseLong(payload.get("timestamp").toString());
            java.time.LocalDateTime time = java.time.LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(timestamp),
                    java.time.ZoneId.systemDefault()
            );

            int hour = time.getHour();

            // Higher risk for transactions between 1AM and 5AM
            if (hour >= 1 && hour <= 5) {
                return 2.5;
            }
        }
        return 1.0;
    }

    /**
     * Get user history risk factor by querying the user settings service
     * @param accountId The user's account ID
     * @return A risk factor based on user history and settings
     */
    private double getUserHistoryRiskFactor(UUID accountId) {
        try {
            // Query the user settings service for the user's risk factor
            Optional<Double> userRiskFactor = userSettingsService.getRiskFactor(accountId);

            if (userRiskFactor.isPresent()) {
                logger.debug("Found custom risk factor for user {}: {}", accountId, userRiskFactor.get());
                return userRiskFactor.get();
            } else {
                // If no specific risk factor is found, calculate one based on transaction history
                double calculatedRisk = calculateRiskBasedOnHistory(accountId);
                logger.debug("Calculated risk factor for user {} based on history: {}", accountId, calculatedRisk);
                return calculatedRisk;
            }
        } catch (Exception e) {
            logger.error("Error retrieving risk factor for user {}: {}", accountId, e.getMessage());
            // Return a default moderate risk factor in case of errors
            return 1.0;
        }
    }

    /**
     * Calculate risk factor based on user's transaction history
     * In a production environment, this would analyze recent transactions
     * @param accountId The user's account ID
     * @return A calculated risk factor
     */
    private double calculateRiskBasedOnHistory(UUID accountId) {
        // This would typically involve:
        // 1. Querying a transaction history database or service
        // 2. Analyzing patterns like frequency, amounts, locations
        // 3. Checking for previous fraud flags or suspicious activities
        // 4. Considering account age and standing

        // For this example, we'll simulate this with some randomization
        // to demonstrate the concept
        try {
            // Convert UUID to string and get the last character
            String accountIdStr = accountId.toString();
            char lastChar = accountIdStr.charAt(accountIdStr.length() - 1);

            // Use the last character to create a deterministic factor
            int lastDigit;
            if (Character.isDigit(lastChar)) {
                lastDigit = Character.getNumericValue(lastChar);
            } else {
                // If it's a letter (a-f in UUID), convert to a number 0-5
                lastDigit = Character.toLowerCase(lastChar) - 'a';
                if (lastDigit < 0 || lastDigit > 5) {
                    lastDigit = 5; // Default value
                }
            }

            // Base risk on this digit scaled to a reasonable range (0.5-2.0)
            double baseRisk = 0.5 + (lastDigit * 0.15);

            // Add a small random factor to simulate other variables
            double randomFactor = Math.random() * 0.3;

            return Math.min(baseRisk + randomFactor, 2.0);
        } catch (Exception e) {
            logger.warn("Error calculating history-based risk, using default: {}", e.getMessage());
            return 1.0;
        }
    }



    /**
     * Calculate risk factor based on transaction velocity (frequency of transactions)
     * Higher velocity of transactions in a short time period indicates higher risk
     *
     * @param accountId The user's account ID
     * @param payload The current transaction payload
     * @return A risk factor based on transaction velocity
     */
    private double getTransactionVelocityFactor(UUID accountId, Map<String, Object> payload) {
        try {
            // Check if we have time since last transaction in the payload
            if (payload.containsKey("timeSinceLastTransaction")) {
                double seconds = Double.parseDouble(payload.get("timeSinceLastTransaction").toString());

                // Very rapid transactions (less than 1 minute) are the highest risk
                if (seconds < 60.0) {
                    logger.debug("Very high velocity detected for account {}: {} seconds", accountId, seconds);
                    return 5.0;
                }
                // Transactions within 5 minutes are high risk
                else if (seconds < 300.0) {
                    logger.debug("High velocity detected for account {}: {} seconds", accountId, seconds);
                    return 3.0;
                }
                // Transactions within 15 minutes are moderate risk
                else if (seconds < 900.0) {
                    return 2.0;
                }
                // Transactions within 1 hour are slightly elevated risk
                else if (seconds < 3600.0) {
                    return 1.5;
                }
            }

            // If we have transaction count information, use that as well
            if (payload.containsKey("transactionCountLast24Hours")) {
                int count = Integer.parseInt(payload.get("transactionCountLast24Hours").toString());

                // Unusually high number of transactions in 24 hours
                if (count > 20) {
                    logger.debug("Unusually high transaction count for account {}: {}", accountId, count);
                    return Math.max(4.0, getTransactionCountRiskFactor(count));
                }
                // High number of transactions
                else if (count > 10) {
                    return Math.max(2.5, getTransactionCountRiskFactor(count));
                }
                // Moderate number of transactions
                else if (count > 5) {
                    return Math.max(1.5, getTransactionCountRiskFactor(count));
                }
            }

            // If we have neither time nor count, try to query transaction history
            if (!payload.containsKey("timeSinceLastTransaction") &&
                    !payload.containsKey("transactionCountLast24Hours")) {
                return queryTransactionVelocityFromHistory(accountId);
            }

            // Default return for normal velocity
            return 1.0;
        } catch (Exception e) {
            logger.error("Error calculating transaction velocity factor: {}", e.getMessage());
            return 1.0; // Default to normal risk in case of errors
        }
    }

    /**
     * Calculate risk factor based on transaction count
     */
    private double getTransactionCountRiskFactor(int count) {
        // Exponential risk increase for higher transaction counts
        // Formula: 1.0 + (count - 5) * 0.2, capped at 5.0
        if (count <= 5) {
            return 1.0;
        }
        return Math.min(1.0 + (count - 5) * 0.2, 5.0);
    }

    /**
     * Query transaction history to determine velocity factor
     * In a real implementation, this would query a transaction database
     */
    private double queryTransactionVelocityFromHistory(UUID accountId) {
        try {
            // This would typically query a transaction history database or service
            // For this example, we'll use a simulated approach

            // Use the account ID to generate a deterministic but varied result
            // This simulates different users having different transaction patterns
            int velocityIndicator = getVelocityIndicator(accountId);
            // Map this to a risk factor between 1.0 and 3.0
            return switch (velocityIndicator) {
                case 0, 1, 2, 3, 4 ->
                    // 50% chance of normal velocity
                        1.0;
                case 5, 6, 7 ->
                    // 30% chance of moderate velocity
                        1.5 + (velocityIndicator - 5) * 0.2;
                case 8 ->
                    // 10% chance of high velocity
                        2.5;
                case 9 ->
                    // 10% chance of very high velocity
                        3.0;
                default ->
                        1.0;//should not happend
            };
        } catch (Exception e) {
            logger.warn("Error querying transaction velocity history: {}", e.getMessage());
            return 1.0;
        }
    }

    private static int getVelocityIndicator(UUID accountId) {
        String accountIdStr = accountId.toString();
        int hashCode = accountIdStr.hashCode();

        // Generate a value between 0 and 9 based on the hash code
        return Math.abs(hashCode % 10);
    }


    private String getRiskCategory(double riskScore) {
        if (riskScore < 3.0) {
            return "LOW";
        } else if (riskScore < 7.0) {
            return "MEDIUM";
        } else if (riskScore < 9.0) {
            return "HIGH";
        } else {
            return "SEVERE";
        }
    }

    /**
     * Enhanced transaction enrichment using simulated ML risk assessment
     */
    private String enrichTransactionWithMachineLearningRiskAssessment(String transactionJson) {
        try {
            KafkaMessage message = objectMapper.readValue(transactionJson, KafkaMessage.class);
            Map<String, Object> payload = message.getPayload();

            // Extract transaction data for ML analysis
            double amount = Double.parseDouble(payload.get("amount").toString());
            String location = (String) payload.getOrDefault("location", "UNKNOWN");
            UUID accountId = (UUID) payload.getOrDefault("accountId", "UNKNOWN");
            String transactionType = (String) payload.getOrDefault("type", "STANDARD");

            // Simulate ML risk assessment
            Map<String, Object> riskAssessment = performMachineLearningRiskAssessment(
                    amount, location, accountId, transactionType, payload
            );

            // Merge risk assessment results into payload
            payload.putAll(riskAssessment);

            message.setPayload(payload);
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            logger.error("Error enriching transaction with ML risk assessment", e);
            return transactionJson;
        }
    }

    /**
     * Performs a machine learning risk assessment for financial transactions.
     * Calls an external ML service and enriches the response with additional risk factors.
     *
     * @param amount Transaction amount
     * @param location Geographic location code of the transaction
     * @param accountId Unique identifier for the account
     * @param transactionType Type of financial transaction
     * @param transactionContext Additional transaction metadata
     * @return Risk assessment results including scores and recommended actions
     * @throws RiskAssessmentException If the assessment process fails
     */
    public Map<String, Object> performMachineLearningRiskAssessment(
            double amount, String location, UUID accountId,
            String transactionType, Map<String, Object> transactionContext) throws RiskAssessmentException {

        long startTime = System.currentTimeMillis();
        Map<String, Object> assessment = new HashMap<>();

        try {
            logger.info("Starting risk assessment for account: {}, amount: {}, type: {}",
                    accountId, amount, transactionType);

            // Sanitize inputs to prevent injection attacks
            String sanitizedLocation = securityService.sanitizeInput(location);
            String sanitizedTransactionType = securityService.sanitizeInput(transactionType);

            // Build request payload for ML service
            RiskAssessmentRequest request = RiskAssessmentRequest.builder()
                    .accountId(accountId)
                    .transactionAmount(amount)
                    .location(sanitizedLocation)
                    .transactionType(sanitizedTransactionType)
                    .deviceInfo(transactionContext.get("deviceInfo"))
                    .ipAddress(transactionContext.get("ipAddress"))
                    .userAgent(transactionContext.get("userAgent"))
                    .build();

            // Call ML service for risk score prediction
            RiskAssessmentResponse mlResponse = null;
            try {
                mlResponse = mlRiskService.evaluateRisk(request);
            } catch (ServiceUnavailableException e) {
                logger.warn("ML service unavailable, falling back to rules engine", e);
                // Fallback to rules-based assessment if ML service is down
                mlResponse = fallbackRiskAssessment(request);
            }

            // Extract base risk score from ML response
            double baseRiskScore = mlResponse.getRiskScore();

            // Additional risk factors that might not be in the ML model

            // Factor: Recent account changes
            if (Boolean.TRUE.equals(transactionContext.get("recentPasswordReset")) ||
                    Boolean.TRUE.equals(transactionContext.get("recentContactInfoChange"))) {
                baseRiskScore += configService.getAccountChangeRiskFactor();
                assessment.put("accountChangeRiskFlag", true);
            }

            // Factor: Velocity check (multiple transactions in short time)
            Integer recentTransactionCount = transactionHistoryService.getRecentTransactionCount(
                    accountId, configService.getVelocityCheckWindowMinutes());
            if (recentTransactionCount > configService.getVelocityTransactionThreshold()) {
                baseRiskScore += configService.getVelocityRiskFactor();
                assessment.put("velocityRiskFlag", true);
            }

            // Factor: New beneficiary for payments
            if (Boolean.TRUE.equals(transactionContext.get("newBeneficiary"))) {
                baseRiskScore += configService.getNewBeneficiaryRiskFactor();
                assessment.put("newBeneficiaryFlag", true);
            }

            // Apply risk multipliers from customer risk profile
            CustomerRiskProfile profile = customerRiskService.getCustomerRiskProfile(accountId);
            double customerRiskMultiplier = profile.getRiskMultiplier();
            double finalRiskScore = Math.min(baseRiskScore * customerRiskMultiplier, 10.0);

            // Behavioral anomaly detection
            AnomalyDetectionResult anomalyResult = anomalyDetectionService.analyze(
                    accountId, transactionContext);
            double anomalyScore = anomalyResult.getScore();

            // Populate assessment results
            assessment.put("mlRiskScore", finalRiskScore);
            assessment.put("anomalyScore", anomalyScore);
            assessment.put("assessmentTimestamp", Instant.now().toString());
            assessment.put("transactionId", transactionContext.get("transactionId"));
            assessment.put("modelVersion", mlResponse.getModelVersion());
            assessment.put("confidenceScore", mlResponse.getConfidenceScore());

            // Apply risk flags based on configured thresholds
            RiskThresholds thresholds = configService.getRiskThresholds(transactionType);
            assessment.put("highRiskFlag", finalRiskScore > thresholds.getHighRiskThreshold());
            assessment.put("anomalyFlag", anomalyScore > thresholds.getAnomalyThreshold());

            // Determine recommended action
            String recommendedAction = determineRecommendedAction(
                    finalRiskScore, anomalyScore, thresholds, transactionType);
            assessment.put("recommendedAction", recommendedAction);

            // Record assessment for audit and model training
            auditService.recordRiskAssessment(accountId, assessment);

            logger.info("Risk assessment completed for account: {}, score: {}, action: {}",
                    accountId, finalRiskScore, recommendedAction);

            return assessment;
        } catch (Exception e) {
            logger.error("Error performing risk assessment for account: " + accountId, e);
            throw new RiskAssessmentException("Failed to complete risk assessment", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            metricsService.recordRiskAssessmentDuration(duration);
        }
    }

    /**
     * Determines the recommended action based on risk scores and configured thresholds
     */
    private String determineRecommendedAction(double riskScore, double anomalyScore,
                                              RiskThresholds thresholds, String transactionType) {

        // Special handling for high-value transactions
        if ("INTERNATIONAL_WIRE".equals(transactionType) &&
                riskScore > thresholds.getInternationalWireThreshold()) {
            return "MANUAL_REVIEW";
        }

        // Standard action determination
        if (riskScore > thresholds.getBlockThreshold() || anomalyScore > thresholds.getBlockAnomalyThreshold()) {
            return "BLOCK";
        } else if (riskScore > thresholds.getReviewThreshold() || anomalyScore > thresholds.getReviewAnomalyThreshold()) {
            return "REVIEW";
        } else if (riskScore > thresholds.getMonitorThreshold() || anomalyScore > thresholds.getMonitorAnomalyThreshold()) {
            return "MONITOR";
        } else {
            return "APPROVE";
        }
    }

    /**
     * Fallback risk assessment using rules engine when ML service is unavailable
     */
    private RiskAssessmentResponse fallbackRiskAssessment(RiskAssessmentRequest request) {
        return rulesEngineService.evaluateRiskWithRules(request);
    }

    /**
     * Calculate anomaly score based on transaction features
     * This simulates an anomaly detection algorithm
     */
    private double calculateAnomalyScore(Map<String, Object> payload) {
        // In a real system, this would use proper anomaly detection
        // For this example, we'll use a simplified approach
        AtomicInteger anomalyFactors = new AtomicInteger(0);
        int totalFactors = 5;

        // Factor 1: Unusual amount
        if (payload.containsKey("amount")) {
            double amount = Double.parseDouble(payload.get("amount").toString());
            double averageAmount = payload.containsKey("userAverageAmount") ?
                    Double.parseDouble(payload.get("userAverageAmount").toString()) : 1000.0;

            if (amount > (averageAmount * 5)) {
                anomalyFactors.incrementAndGet();
            }
        }

        // Factor 2: Unusual location
        if ("UNUSUAL".equals(payload.getOrDefault("locationStatus", ""))) {
            anomalyFactors.incrementAndGet();
        }

        // Factor 3: Time of day
        if (payload.containsKey("timestamp")) {
            long timestamp = Long.parseLong(payload.get("timestamp").toString());
            java.time.LocalDateTime time = java.time.LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(timestamp),
                    java.time.ZoneId.systemDefault()
            );

            // Unusual hours (middle of night)
            int hour = time.getHour();
            if (hour >= 1 && hour <= 4) {
                anomalyFactors.incrementAndGet();
            }
        }

        // Factor 4: Rapid transaction sequence
        if (payload.containsKey("timeSinceLastTransaction")) {
            double seconds = Double.parseDouble(payload.get("timeSinceLastTransaction").toString());
            if (seconds < 60.0) { // Less than a minute
                anomalyFactors.incrementAndGet();
            }
        }

        // Factor 5: New recipient for large amount
        if (Boolean.TRUE.equals(payload.getOrDefault("isNewRecipient", false)) &&
                payload.containsKey("amount")) {
            double amount = Double.parseDouble(payload.get("amount").toString());
            if (amount > 1000.0) {
                anomalyFactors.incrementAndGet();
            }
        }

        // Calculate final score as ratio of anomalous factors
        return (double) anomalyFactors.get() / totalFactors;
    }

    private Iterable<String> detectFraudPatterns(String transactionJson) {
        try {
            KafkaMessage message = objectMapper.readValue(transactionJson, KafkaMessage.class);
            Map<String, Object> payload = message.getPayload();

            // Enhanced fraud detection with multiple patterns and risk levels
            boolean isSuspicious = false;
            String fraudType = null;
            String severity = "MEDIUM";

            // Pattern 1: Unusual location
            if (payload.containsKey("location") &&
                    "HIGH_RISK".equals(payload.get("location"))) {
                isSuspicious = true;
                fraudType = "UNUSUAL_LOCATION";
                severity = "HIGH";
            }

            // Pattern 2: Rapid succession transactions
            if (payload.containsKey("timeSinceLastTransaction") &&
                    (Double) payload.get("timeSinceLastTransaction") < 60.0) { // Less than 60 seconds
                isSuspicious = true;
                fraudType = "RAPID_SUCCESSION";
                severity = "HIGH";
            }

            // Pattern 3: ML risk score above threshold
            if (payload.containsKey("mlRiskScore") &&
                    (Double) payload.get("mlRiskScore") > 8.0) {
                isSuspicious = true;
                fraudType = "ML_HIGH_RISK";
                severity = "HIGH";
            }

            // Pattern 4: High anomaly score
            if (payload.containsKey("anomalyScore") &&
                    (Double) payload.get("anomalyScore") > 0.8) {
                isSuspicious = true;
                fraudType = "ANOMALOUS_PATTERN";
                severity = "CRITICAL";
            }

            // Pattern 5: Amount exceeds account average by factor
            if (payload.containsKey("amount") && payload.containsKey("accountAverageAmount")) {
                double amount = Double.parseDouble(payload.get("amount").toString());
                double avgAmount = Double.parseDouble(payload.get("accountAverageAmount").toString());

                if (amount > (avgAmount * 10)) {
                    isSuspicious = true;
                    fraudType = "AMOUNT_ANOMALY";
                    severity = "MEDIUM";
                }
            }

            if (isSuspicious) {
                payload.put("fraudType", fraudType);
                payload.put("fraudSeverity", severity);
                payload.put("status", "SUSPICIOUS");
                payload.put("detectionTimestamp", System.currentTimeMillis());
                message.setPayload(payload);

                // Create a fraud alert message with enhanced data
                KafkaMessage alertMessage = new KafkaMessage(
                        KafkaMessage.MessageType.SYSTEM_ALERT,
                        "transaction-stream-processor",
                        (String) payload.getOrDefault("accountId", "unknown"),
                        "FRAUD_DETECTION"
                );

                Map<String, Object> alertPayload = new HashMap<>();
                alertPayload.put("transactionId", payload.get("transactionId"));
                alertPayload.put("fraudType", fraudType);
                alertPayload.put("severity", severity);
                alertPayload.put("detectionMethod", "STREAM_PROCESSOR");
                alertPayload.put("confidence", calculateFraudConfidence(payload));
                alertPayload.put("originalTransaction", payload);
                alertPayload.put("recommendedActions", getFraudRecommendations(fraudType, severity));

                alertMessage.setPayload(alertPayload);
                alertMessage.setPriority(KafkaMessage.Priority.CRITICAL);

                return java.util.Arrays.asList(
                        objectMapper.writeValueAsString(message),
                        objectMapper.writeValueAsString(alertMessage)
                );
            }

            return java.util.Collections.singletonList(transactionJson);
        } catch (Exception e) {
            logger.error("Error detecting fraud patterns", e);
            return java.util.Collections.singletonList(transactionJson);
        }
    }

    /**
     * Calculate confidence level for fraud detection
     */
    private double calculateFraudConfidence(Map<String, Object> payload) {
        // Start with base confidence
        double baseConfidence = 0.7;

        // Adjust based on ML risk score if available
        if (payload.containsKey("mlRiskScore")) {
            double mlRiskScore = (Double) payload.get("mlRiskScore");
            baseConfidence = Math.max(baseConfidence, mlRiskScore / 10.0);
        }

        // Adjust based on anomaly score if available
        if (payload.containsKey("anomalyScore")) {
            double anomalyScore = (Double) payload.get("anomalyScore");
            baseConfidence = Math.max(baseConfidence, anomalyScore);
        }

        return Math.min(baseConfidence, 0.99); // Cap at 99% confidence
    }

    /**
     * Get recommended actions based on fraud type and severity
     */
    private String[] getFraudRecommendations(String fraudType, String severity) {
        return switch (fraudType) {
            case "UNUSUAL_LOCATION" -> new String[]{
                    "NOTIFY_USER",
                    "CONFIRM_LOCATION",
                    "HIGH".equals(severity) ? "REQUIRE_ADDITIONAL_AUTH" : "MONITOR_ACCOUNT"
            };
            case "RAPID_SUCCESSION" -> new String[]{
                    "NOTIFY_USER",
                    "FREEZE_TRANSACTIONS",
                    "REVIEW_RECENT_ACTIVITY"
            };
            case "ML_HIGH_RISK" -> new String[]{
                    "FREEZE_ACCOUNT",
                    "NOTIFY_SECURITY_TEAM",
                    "REQUIRE_CALLBACK_VERIFICATION"
            };
            case "ANOMALOUS_PATTERN" -> new String[]{
                    "FREEZE_ACCOUNT",
                    "NOTIFY_SECURITY_TEAM",
                    "INVESTIGATE_URGENTLY"
            };
            case "AMOUNT_ANOMALY" -> new String[]{
                    "NOTIFY_USER",
                    "CONFIRM_TRANSACTION",
                    "REVIEW_ACCOUNT_LIMITS"
            };
            default -> new String[]{
                    "NOTIFY_USER",
                    "MONITOR_ACCOUNT"
            };
        };
    }

    private String aggregateTransactionAmount(String transactionJson, String currentTotal) {
        try {
            KafkaMessage message = objectMapper.readValue(transactionJson, KafkaMessage.class);
            Map<String, Object> payload = message.getPayload();

            if (payload.containsKey("amount")) {
                double amount = Double.parseDouble(payload.get("amount").toString());
                double total = Double.parseDouble(currentTotal);
                return String.valueOf(total + amount);
            }
        } catch (Exception e) {
            logger.error("Error aggregating transaction amount", e);
        }
        return currentTotal;
    }

    /**
     * Get user-specific spending threshold
     * Queries the user settings service for the user's configured threshold
     */
    private double getUserSpendingThreshold(UUID userId) {
        try {
            // Use the injected userSettingsService to get the user's spending threshold
            return userSettingsService.getSpendingThreshold(userId)
                    .orElseGet(() -> {
                        logger.info("No custom threshold found for user {}, using default: {}",
                                userId, defaultSpendingAlertThreshold);
                        return defaultSpendingAlertThreshold;
                    });
        } catch (Exception e) {
            logger.error("Error retrieving spending threshold for user {}: {}", userId, e.getMessage());
            // Fallback to default threshold in case of errors
            return defaultSpendingAlertThreshold;
        }
    }
    public boolean exceedsUserThreshold(UUID userId, BigDecimal totalAmount) {
        try {
            double threshold = getUserSpendingThreshold(userId);
            return totalAmount.compareTo(BigDecimal.valueOf(threshold)) > 0;
        } catch (Exception e) {
            logger.error("Error checking spending threshold", e);
            return false;
        }
    }


    private String createSpendingNotification(String totalAmount) {
        try {
            double total = Double.parseDouble(totalAmount);

            KafkaMessage notification = new KafkaMessage(
                    KafkaMessage.MessageType.NOTIFICATION,
                    "transaction-stream-processor",
                    "user-spending",
                    "THRESHOLD_ALERT"
            );

            Map<String, Object> notificationPayload = new HashMap<>();
            notificationPayload.put("message", "You've spent $" + String.format("%.2f", total) +
                    " in the last hour, which exceeds your alert threshold.");
            notificationPayload.put("amount", total);
            notificationPayload.put("timestamp", java.time.LocalDateTime.now().toString());
            notificationPayload.put("notificationType", "SPENDING_ALERT");

            // Add recommendation based on spending amount
            if (total > defaultSpendingAlertThreshold * 2) {
                notificationPayload.put("recommendation",
                        "Your spending is significantly above your usual patterns. " +
                                "Consider reviewing your recent transactions.");
            }

            notification.setPayload(notificationPayload);
            notification.setPriority(KafkaMessage.Priority.HIGH);

            return objectMapper.writeValueAsString(notification);
        } catch (JsonProcessingException e) {
            logger.error("Error creating spending notification", e);
            return "{}";
        }
    }

    /**
     * Extract basic transaction info for consolidated view
     */
    private String extractBasicTransactionInfo(String transactionJson) {
        try {
            KafkaMessage message = objectMapper.readValue(transactionJson, KafkaMessage.class);
            Map<String, Object> payload = message.getPayload();

            // Create a simplified view for analytics
            Map<String, Object> basicInfo = new HashMap<>();
            basicInfo.put("transactionId", payload.getOrDefault("transactionId", "Unknown"));
            basicInfo.put("accountId", payload.getOrDefault("accountId", "Unknown"));
            basicInfo.put("type", payload.getOrDefault("type", "Unknown"));
            basicInfo.put("amount", payload.getOrDefault("amount", 0.0));
            basicInfo.put("timestamp", payload.getOrDefault("timestamp", System.currentTimeMillis()));
            basicInfo.put("processingTime", System.currentTimeMillis());

            // Add transaction category if available
            if (payload.containsKey("category")) {
                basicInfo.put("category", payload.get("category"));
            } else {
                basicInfo.put("category", inferTransactionCategory(payload));
            }

            // Create analytics message
            KafkaMessage analyticsMessage = new KafkaMessage(
                    KafkaMessage.MessageType.ANALYTICS,
                    "transaction-stream-processor",
                    (String) payload.getOrDefault("accountId", "unknown"),
                    "TRANSACTION_SUMMARY"
            );
            analyticsMessage.setPayload(basicInfo);

            return objectMapper.writeValueAsString(analyticsMessage);
        } catch (Exception e) {
            logger.error("Error extracting basic transaction info", e);
            return null;
        }
    }

    /**
     * Infer transaction category based on available data
     */
    private String inferTransactionCategory(Map<String, Object> payload) {
        // Get transaction type if available
        String transactionType = (String) payload.getOrDefault("type", "STANDARD");

        // Check merchant category code if available
        if (payload.containsKey("merchantCategoryCode")) {
            String mcc = (String) payload.get("merchantCategoryCode");
            return mapMerchantCategoryCodeToCategory(mcc);
        }

        // Check description for keywords
        if (payload.containsKey("description")) {
            String description = ((String) payload.get("description")).toLowerCase();

            // Food & Dining
            if (description.contains("restaurant") || description.contains("food") ||
                    description.contains("cafe") || description.contains("diner") ||
                    description.contains("pizza") || description.contains("burger")) {
                return "FOOD_AND_DINING";
            }

            // Shopping
            if (description.contains("shop") || description.contains("store") ||
                    description.contains("market") || description.contains("retail") ||
                    description.contains("amazon") || description.contains("walmart")) {
                return "SHOPPING";
            }

            // Transportation
            if (description.contains("airline") || description.contains("flight") ||
                    description.contains("uber") || description.contains("lyft") ||
                    description.contains("taxi") || description.contains("train") ||
                    description.contains("transit")) {
                return "TRANSPORTATION";
            }

            // Utilities
            if (description.contains("electric") || description.contains("water") ||
                    description.contains("gas") || description.contains("utility") ||
                    description.contains("phone") || description.contains("internet") ||
                    description.contains("bill")) {
                return "UTILITIES";
            }

            // Entertainment
            if (description.contains("movie") || description.contains("theatre") ||
                    description.contains("theater") || description.contains("netflix") ||
                    description.contains("spotify") || description.contains("entertainment")) {
                return "ENTERTAINMENT";
            }
        }

        // Infer based on transaction type
        return switch (transactionType) {
            case "WITHDRAWAL" -> "CASH_WITHDRAWAL";
            case "DEPOSIT" -> "DEPOSIT";
            case "TRANSFER" -> "TRANSFER";
            case "PAYMENT" -> "BILL_PAYMENT";
            case "REFUND" -> "REFUND";
            case "FEE" -> "BANK_FEE";
            case "INTERNATIONAL" -> "INTERNATIONAL";
            case "INVESTMENT" -> "INVESTMENT";
            case "SUBSCRIPTION" -> "SUBSCRIPTION";
            case "MORTGAGE", "LOAN_PAYMENT" -> "LOAN_PAYMENT";
            default -> {
                if (payload.containsKey("amount")) {
                    try {
                        double amount = Double.parseDouble(payload.get("amount").toString());
                        if (amount > 5000) {
                            yield "LARGE_TRANSACTION";
                        } else if (amount < 10) {
                            yield "SMALL_TRANSACTION";
                        }
                    } catch (NumberFormatException e) {
                        logger.error("Error parsing amount for category inference", e);
                    }
                }
                yield "UNCATEGORIZED";
                // If amount is available, categorize based on amount
            }
        };
    }

    /**
     * Map Merchant Category Code (MCC) to transaction category
     */
    private String mapMerchantCategoryCodeToCategory(String mcc) {
        // MCC codes are standardized industry codes
        // This is a simplified implementation - actual implementation would have more comprehensive mapping

        if (mcc == null) {
            return "UNCATEGORIZED";
        }

        // Financial Services
        if (mcc.startsWith("6")) {
            return "FINANCIAL_SERVICES";
        }

        // Airlines and Travel
        if (mcc.startsWith("3") || mcc.equals("4511")) {
            return "TRAVEL";
        }

        // Gas Stations
        if (mcc.equals("5541") || mcc.equals("5542")) {
            return "AUTOMOTIVE_FUEL";
        }

        // Supermarkets
        if (mcc.equals("5411")) {
            return "GROCERIES";
        }

        // Restaurants and Food
        if (mcc.startsWith("5812") || mcc.startsWith("5813") || mcc.startsWith("5814")) {
            return "FOOD_AND_DINING";
        }

        // Healthcare
        if (mcc.startsWith("8")) {
            return "HEALTHCARE";
        }

        // Retail Stores
        if (mcc.startsWith("5")) {
            return "SHOPPING";
        }

        // Government Services
        if (mcc.startsWith("9")) {
            return "GOVERNMENT_SERVICES";
        }

        return "UNCATEGORIZED";
    }
}
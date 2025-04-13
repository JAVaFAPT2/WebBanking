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
import org.springframework.stereotype.Component;
import service.messagebroker.models.KafkaMessage;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Streams processor for transaction data
 * Processes transaction events for fraud detection, analytics, and notifications
 */
@Component
@EnableKafkaStreams
public class TransactionStreamProcessor {
    private static final Logger logger = LoggerFactory.getLogger(TransactionStreamProcessor.class);

    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.topics.transaction}")
    private String transactionTopic;

    @Value("${spring.kafka.topics.notification}")
    private String notificationTopic;

    @Value("${spring.kafka.topics.analytics}")
    private String analyticsTopic;

    @Value("${spring.kafka.topics.fraud-alert}")
    private String fraudAlertTopic;

    @Autowired
    public TransactionStreamProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
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

        // Create a peeked stream for logging
        KStream<String, String> peekedStream = transactionStream
                .peek((key, value) -> logger.info("Processing transaction: {}", key));

        // Branch the stream based on transaction type
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
                .to(analyticsTopic, Produced.with(Serdes.String(), Serdes.String()));

        // Detect potential fraud in international transactions
        internationalTransactions
                .flatMapValues(this::detectFraudPatterns)
                .filter((key, value) -> value != null && value.contains("SUSPICIOUS"))
                .to(fraudAlertTopic, Produced.with(Serdes.String(), Serdes.String()));

        // Aggregate transaction amounts by user for real-time spending analysis
        regularTransactions
                .groupByKey()
                .windowedBy(TimeWindows.of(Duration.ofHours(1)))
                .aggregate(
                        () -> "0.0", // Initial value
                        (key, value, aggregate) -> aggregateTransactionAmount(value, aggregate),
                        Materialized.with(Serdes.String(), Serdes.String())
                )
                .toStream()
                .map((windowedKey, value) -> KeyValue.pair(windowedKey.key(), value))
                .filter((key, value) -> exceedsUserThreshold(key, value))
                .mapValues(this::createSpendingNotification)
                .to(notificationTopic, Produced.with(Serdes.String(), Serdes.String()));

        return transactionStream;
    }

    private boolean isHighValueTransaction(String transactionJson) {
        try {
            KafkaMessage message = objectMapper.readValue(transactionJson, KafkaMessage.class);
            Map<String, Object> payload = message.getPayload();
            if (payload.containsKey("amount")) {
                double amount = Double.parseDouble(payload.get("amount").toString());
                return amount > 10000.0;
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

            double riskScore = calculateRiskScore(amount, location);
            payload.put("riskScore", riskScore);

            message.setPayload(payload);
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            logger.error("Error enriching transaction with risk score", e);
            return transactionJson;
        }
    }

    private double calculateRiskScore(double amount, String location) {
        // Simple risk calculation algorithm
        double baseScore = amount / 1000.0;

        // Higher risk for certain locations
        if ("INTERNATIONAL".equals(location)) {
            baseScore *= 1.5;
        }

        return Math.min(baseScore, 10.0); // Cap at 10
    }

    private Iterable<String> detectFraudPatterns(String transactionJson) {
        try {
            KafkaMessage message = objectMapper.readValue(transactionJson, KafkaMessage.class);
            Map<String, Object> payload = message.getPayload();

            // Check for fraud patterns
            boolean isSuspicious = false;
            String fraudType = null;

            // Pattern 1: Unusual location
            if (payload.containsKey("location") &&
                    "HIGH_RISK".equals(payload.get("location"))) {
                isSuspicious = true;
                fraudType = "UNUSUAL_LOCATION";
            }

            // Pattern 2: Rapid succession transactions
            if (payload.containsKey("timeSinceLastTransaction") &&
                    (Double)payload.get("timeSinceLastTransaction") < 60.0) { // Less than 60 seconds
                isSuspicious = true;
                fraudType = "RAPID_SUCCESSION";
            }

            if (isSuspicious) {
                payload.put("fraudType", fraudType);
                payload.put("status", "SUSPICIOUS");
                message.setPayload(payload);

                // Create a fraud alert message
                KafkaMessage alertMessage = new KafkaMessage(
                        KafkaMessage.MessageType.SYSTEM_ALERT,
                        "transaction-stream-processor",
                        (String) payload.get("accountId"),
                        "FRAUD_DETECTION"
                );

                Map<String, Object> alertPayload = new HashMap<>();
                alertPayload.put("transactionId", payload.get("transactionId"));
                alertPayload.put("fraudType", fraudType);
                alertPayload.put("severity", "HIGH");
                alertPayload.put("originalTransaction", payload);

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

    private boolean exceedsUserThreshold(String userId, String totalAmount) {
        try {
            double total = Double.parseDouble(totalAmount);
            // This could be more sophisticated with user-specific thresholds from a database
            double threshold = 5000.0; // Default threshold

            return total > threshold;
        } catch (Exception e) {
            logger.error("Error checking user threshold", e);
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

            notification.setPayload(notificationPayload);
            notification.setPriority(KafkaMessage.Priority.HIGH);

            return objectMapper.writeValueAsString(notification);
        } catch (JsonProcessingException e) {
            logger.error("Error creating spending notification", e);
            return "{}";
        }
    }
}

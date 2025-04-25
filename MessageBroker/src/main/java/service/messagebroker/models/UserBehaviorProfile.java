package service.messagebroker.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Model representing a user's transaction behavior patterns
 * Used for anomaly detection and behavioral analysis
 */
@Data
@Builder
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "user_behavior_profiles")
public class UserBehaviorProfile {
    /**
     * Unique identifier for the user
     */
    @Id
    private  UUID userId = UUID.randomUUID();

    /**
     * Average transaction amount for this user
     */
    private double averageTransactionAmount;

    /**
     * Standard deviation of transaction amounts
     */
    private double amountStandardDeviation;

    /**
     * Frequency of transactions by hour of day (24-hour format)
     * Index 0 = midnight (12am), index 23 = 11pm
     * Stored as a comma-separated string in the database
     */
    @Column(length = 100)
    private String hourFrequencyData;

    /**
     * Frequency of transactions by location
     * Stored as JSON in the database
     */
    @Column(columnDefinition = "TEXT")
    private String locationFrequencyData;

    /**
     * Total number of transactions analyzed
     */
    private int transactionCount;

    /**
     * Running sum of transaction amounts (for calculating average)
     */
    private double transactionAmountSum;

    /**
     * Running sum of squared differences (for calculating standard deviation)
     */
    private double squaredDifferenceSum;

    /**
     * Timestamp when this profile was last updated
     */
    private Instant lastUpdated;

    /**
     * Transient (non-persisted) field for hourFrequency
     */
    @Transient
    private int[] hourFrequency;

    /**
     * Transient (non-persisted) field for locationFrequency
     */
    @Transient
    private Map<String, Integer> locationFrequency;

    /**
     * Creates a new empty profile for a user
     *
     * @param userId The user's unique identifier
     * @return A new empty behavior profile
     */
    public static UserBehaviorProfile createEmptyProfile(UUID userId) {
        UserBehaviorProfile profile = UserBehaviorProfile.builder()
                .userId(userId)
                .averageTransactionAmount(0.0)
                .amountStandardDeviation(0.0)
                .transactionCount(0)
                .transactionAmountSum(0.0)
                .squaredDifferenceSum(0.0)
                .lastUpdated(Instant.now())
                .build();

        profile.setHourFrequency(new int[24]);
        profile.setLocationFrequency(new HashMap<>());

        return profile;
    }

    /**
     * Updates the profile with a new transaction
     *
     * @param amount The transaction amount
     * @param timestamp The transaction timestamp
     * @param location The transaction location
     */
    public void updateWithTransaction(double amount, long timestamp, String location) {
        // Ensure transient fields are initialized
        initializeTransientFields();

        // Update transaction count
        transactionCount++;

        // Update amount statistics
        double oldAverage = averageTransactionAmount;
        transactionAmountSum += amount;
        averageTransactionAmount = transactionAmountSum / transactionCount;

        // Update standard deviation using Welford's online algorithm
        if (transactionCount > 1) {
            double delta = amount - oldAverage;
            double delta2 = amount - averageTransactionAmount;
            squaredDifferenceSum += delta * delta2;
            amountStandardDeviation = Math.sqrt(squaredDifferenceSum / (transactionCount - 1));
        }

        // Update hour frequency
        if (timestamp > 0) {
            LocalDateTime time = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
            int hour = time.getHour();
            hourFrequency[hour]++;
            updateHourFrequencyData();
        }

        // Update location frequency
        if (location != null && !location.isEmpty()) {
            locationFrequency.put(location, locationFrequency.getOrDefault(location, 0) + 1);
            updateLocationFrequencyData();
        }

        // Update timestamp
        lastUpdated = Instant.now();
    }

    /**
     * Gets the most common hour for transactions
     *
     * @return The hour (0-23) with the most transactions
     */
    public int getMostCommonHour() {
        initializeTransientFields();

        int maxCount = -1;
        int mostCommonHour = 0;
        for (int hour = 0; hour < 24; hour++) {
            if (hourFrequency[hour] > maxCount) {
                maxCount = hourFrequency[hour];
                mostCommonHour = hour;
            }
        }
        return mostCommonHour;
    }

    /**
     * Gets the most common location for transactions
     *
     * @return The most common location or "UNKNOWN" if none
     */
    public String getMostCommonLocation() {
        initializeTransientFields();

        if (locationFrequency.isEmpty()) {
            return "UNKNOWN";
        }

        String mostCommonLocation = null;
        int maxCount = -1;
        for (Map.Entry<String, Integer> entry : locationFrequency.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostCommonLocation = entry.getKey();
            }
        }
        return mostCommonLocation != null ? mostCommonLocation : "UNKNOWN";
    }

    /**
     * Checks if the profile has enough data for reliable anomaly detection
     *
     * @return True if the profile has sufficient data, false otherwise
     */
    public boolean hasSufficientData() {
        return transactionCount >= 5;
    }

    /**
     * Gets the probability of a transaction at a specific hour
     *
     * @param hour The hour to check (0-23)
     * @return The probability (0.0-1.0) of transactions at this hour
     */
    public double getHourProbability(int hour) {
        initializeTransientFields();

        if (transactionCount == 0 || hour < 0 || hour >= 24) {
            return 0.0;
        }
        return (double) hourFrequency[hour] / transactionCount;
    }

    /**
     * Gets the probability of a transaction at a specific location
     *
     * @param location The location to check
     * @return The probability (0.0-1.0) of transactions at this location
     */
    public double getLocationProbability(String location) {
        initializeTransientFields();

        if (transactionCount == 0 || location == null || !locationFrequency.containsKey(location)) {
            return 0.0;
        }
        return (double) locationFrequency.get(location) / transactionCount;
    }

    /**
     * Merges another profile into this one
     * Useful when combining profiles from different sources
     *
     * @param other The other profile to merge
     */
    public void mergeWith(UserBehaviorProfile other) {
        if (other == null || !userId.equals(other.getUserId())) {
            return;
        }

        // Ensure transient fields are initialized
        initializeTransientFields();
        other.initializeTransientFields();

        // Merge transaction counts and sums
        int newTransactionCount = this.transactionCount + other.getTransactionCount();
        double newTransactionSum = this.transactionAmountSum + other.getTransactionAmountSum();

        // Calculate new average
        double newAverage = newTransactionCount > 0 ? newTransactionSum / newTransactionCount : 0.0;

        // Merge hour frequencies
        int[] newHourFrequency = new int[24];
        for (int i = 0; i < 24; i++) {
            newHourFrequency[i] = this.hourFrequency[i] + other.getHourFrequency()[i];
        }

        // Merge location frequencies
        Map<String, Integer> newLocationFrequency = new HashMap<>(this.locationFrequency);
        for (Map.Entry<String, Integer> entry : other.getLocationFrequency().entrySet()) {
            newLocationFrequency.put(
                    entry.getKey(),
                    newLocationFrequency.getOrDefault(entry.getKey(), 0) + entry.getValue()
            );
        }

        // Update this profile
        this.transactionCount = newTransactionCount;
        this.transactionAmountSum = newTransactionSum;
        this.averageTransactionAmount = newAverage;
        this.hourFrequency = newHourFrequency;
        this.locationFrequency = newLocationFrequency;

        // Update the persisted string representations
        updateHourFrequencyData();
        updateLocationFrequencyData();

        this.lastUpdated = Instant.now();

        // Recalculate standard deviation (simplified approach)
        // Note: This is an approximation as proper merging of standard deviations is complex
        if (this.transactionCount > 1 && other.getTransactionCount() > 1) {
            double weightedStdDev = (this.amountStandardDeviation * (this.transactionCount - 1) +
                    other.getAmountStandardDeviation() * (other.getTransactionCount() - 1)) /
                    (newTransactionCount - 2);
            this.amountStandardDeviation = weightedStdDev;
        }
    }

    /**
     * Initializes the transient fields from the persisted data
     */
    @PostLoad
    public void initializeTransientFields() {
        // Initialize hour frequency array
        if (hourFrequency == null) {
            hourFrequency = new int[24];
            if (hourFrequencyData != null && !hourFrequencyData.isEmpty()) {
                String[] parts = hourFrequencyData.split(",");
                for (int i = 0; i < Math.min(parts.length, 24); i++) {
                    try {
                        hourFrequency[i] = Integer.parseInt(parts[i]);
                    } catch (NumberFormatException e) {
                        hourFrequency[i] = 0;
                    }
                }
            }
        }

        // Initialize location frequency map
        if (locationFrequency == null) {
            locationFrequency = new HashMap<>();
            if (locationFrequencyData != null && !locationFrequencyData.isEmpty()) {
                try {
                    // Simple parsing of the format: location1=count1;location2=count2;...
                    String[] entries = locationFrequencyData.split(";");
                    for (String entry : entries) {
                        String[] keyValue = entry.split("=");
                        if (keyValue.length == 2) {
                            locationFrequency.put(keyValue[0], Integer.parseInt(keyValue[1]));
                        }
                    }
                } catch (Exception e) {
                    // If parsing fails, start with an empty map
                    locationFrequency = new HashMap<>();
                }
            }
        }
    }

    /**
     * Updates the persisted hour frequency data from the transient array
     */
    private void updateHourFrequencyData() {
        if (hourFrequency != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < hourFrequency.length; i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(hourFrequency[i]);
            }
            hourFrequencyData = sb.toString();
        }
    }

    /**
     * Updates the persisted location frequency data from the transient map
     */
    private void updateLocationFrequencyData() {
        if (locationFrequency != null) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, Integer> entry : locationFrequency.entrySet()) {
                if (!first) {
                    sb.append(";");
                }
                sb.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
            locationFrequencyData = sb.toString();
        }
    }

    /**
     * Gets the hour frequency array
     * Ensures the transient field is initialized
     */
    public int[] getHourFrequency() {
        initializeTransientFields();
        return hourFrequency;
    }

    /**
     * Gets the location frequency map
     * Ensures the transient field is initialized
     */
    public Map<String, Integer> getLocationFrequency() {
        initializeTransientFields();
        return locationFrequency;
    }

    /**
     * Sets the hour frequency array and updates the persisted data
     */
    public void setHourFrequency(int[] hourFrequency) {
        this.hourFrequency = hourFrequency;
        updateHourFrequencyData();
    }

    /**
     * Sets the location frequency map and updates the persisted data
     */
    public void setLocationFrequency(Map<String, Integer> locationFrequency) {
        this.locationFrequency = locationFrequency;
        updateLocationFrequencyData();
    }
}

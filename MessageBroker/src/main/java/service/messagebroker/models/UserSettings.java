package service.messagebroker.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


import java.util.UUID;

/**
 * Entity representing user settings for the banking application
 * Contains user preferences and configuration settings
 */
@Entity
@Table(name = "user_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings {

    /**
     * Unique identifier for the settings record
     */
    @Id
    @GeneratedValue
    private UUID id = UUID.randomUUID();

    /**
     * The user ID associated with these settings
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    /**
     * User's preferred spending alert threshold
     * Alerts are triggered when spending exceeds this amount
     */
    @Column(name = "spending_threshold")
    private Double spendingThreshold;

    /**
     * User's custom risk factor
     * Used to adjust risk calculations based on user preferences
     */
    @Column(name = "risk_factor")
    private Double riskFactor;

    /**
     * Flag indicating if the user wants to receive notifications
     */
    @Column(name = "notifications_enabled")
    private Boolean notificationsEnabled;

    /**
     * User's preferred notification channel (EMAIL, SMS, PUSH, etc.)
     */
    @Column(name = "notification_channel")
    private String notificationChannel;

    /**
     * Flag indicating if the user wants to receive transaction alerts
     */
    @Column(name = "transaction_alerts_enabled")
    private Boolean transactionAlertsEnabled;

    /**
     * Minimum transaction amount that triggers alerts
     */
    @Column(name = "transaction_alert_threshold")
    private Double transactionAlertThreshold;

    /**
     * Timestamp when the settings were last updated
     */
    @Column(name = "last_updated")
    private Long lastUpdated;

    /**
     * User's preferred language for notifications and UI
     */
    @Column(name = "preferred_language")
    private String preferredLanguage;

    /**
     * User's preferred currency for displaying amounts
     */
    @Column(name = "preferred_currency")
    private String preferredCurrency;
}

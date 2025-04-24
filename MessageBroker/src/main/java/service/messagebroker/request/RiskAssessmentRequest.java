package service.messagebroker.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Request object for machine learning risk assessment service.
 * Contains all data necessary for assessing transaction risk.
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
public class RiskAssessmentRequest {

    /**
     * Unique identifier of the account performing the transaction
     */
    private final UUID accountId;

    /**
     * Amount of the transaction
     */
    private final double transactionAmount;

    /**
     * Geographic location code of the transaction
     */
    private final String location;

    /**
     * Type of financial transaction (e.g., DOMESTIC_TRANSFER, INTERNATIONAL_WIRE)
     */
    private final String transactionType;

    /**
     * Device information from which the transaction was initiated
     */
    private final Object deviceInfo;

    /**
     * IP address from which the transaction was initiated
     */
    private final Object ipAddress;

    /**
     * User agent string from the browser/device
     */
    private final Object userAgent;

    /**
     * Unique identifier for the transaction
     */
    private final String transactionId;

    /**
     * Timestamp when the transaction was initiated (in ISO-8601 format)
     */
    private final String transactionTimestamp;

    /**
     * Currency code for the transaction (ISO 4217)
     */
    private final String currencyCode;

    /**
     * Payment method used (if applicable)
     */
    private final String paymentMethod;

    /**
     * Details about the transaction beneficiary
     */
    private final BeneficiaryInfo beneficiaryInfo;

    /**
     * Customer tenure in days
     */
    private final int customerTenureDays;

    /**
     * Additional transaction attributes for risk assessment
     */
    private final Map<String, Object> additionalAttributes;

    /**
     * Private constructor used by the builder
     */
    private RiskAssessmentRequest(Builder builder) {
        this.accountId = builder.accountId;
        this.transactionAmount = builder.transactionAmount;
        this.location = builder.location;
        this.transactionType = builder.transactionType;
        this.deviceInfo = builder.deviceInfo;
        this.ipAddress = builder.ipAddress;
        this.userAgent = builder.userAgent;
        this.transactionId = builder.transactionId;
        this.transactionTimestamp = builder.transactionTimestamp;
        this.currencyCode = builder.currencyCode;
        this.paymentMethod = builder.paymentMethod;
        this.beneficiaryInfo = builder.beneficiaryInfo;
        this.customerTenureDays = builder.customerTenureDays;
        this.additionalAttributes = builder.additionalAttributes;
    }

    /**
     * Creates a new builder for RiskAssessmentRequest
     *
     * @return A new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    // Getters for all properties


    public Map<String, Object> getAdditionalAttributes() {
        return Collections.unmodifiableMap(additionalAttributes);
    }

    /**
     * Checks if the request has a specific additional attribute
     *
     * @param attributeName Name of the attribute
     * @return true if the attribute exists
     */
    public boolean hasAttribute(String attributeName) {
        return additionalAttributes.containsKey(attributeName);
    }

    /**
     * Gets an additional attribute value
     *
     * @param attributeName Name of the attribute
     * @return The attribute value or null if not present
     */
    public Object getAttribute(String attributeName) {
        return additionalAttributes.get(attributeName);
    }

    /**
     * Builder class for creating RiskAssessmentRequest instances
     */
    public static class Builder {
        private UUID accountId;
        private double transactionAmount;
        private String location;
        private String transactionType;
        private Object deviceInfo;
        private Object ipAddress;
        private Object userAgent;
        private String transactionId;
        private String transactionTimestamp;
        private String currencyCode = "USD"; // Default
        private String paymentMethod;
        private BeneficiaryInfo beneficiaryInfo;
        private int customerTenureDays;
        private Map<String, Object> additionalAttributes = new HashMap<>();

        /**
         * Sets the account ID
         *
         * @param accountId Account identifier
         * @return This builder instance
         */
        public Builder accountId(UUID accountId) {
            this.accountId = accountId;
            return this;
        }

        /**
         * Sets the transaction amount
         *
         * @param transactionAmount Amount of the transaction
         * @return This builder instance
         */
        public Builder transactionAmount(double transactionAmount) {
            this.transactionAmount = transactionAmount;
            return this;
        }

        /**
         * Sets the location
         *
         * @param location Geographic location code
         * @return This builder instance
         */
        public Builder location(String location) {
            this.location = location;
            return this;
        }

        /**
         * Sets the transaction type
         *
         * @param transactionType Type of transaction
         * @return This builder instance
         */
        public Builder transactionType(String transactionType) {
            this.transactionType = transactionType;
            return this;
        }

        /**
         * Sets the device info
         *
         * @param deviceInfo Device information object
         * @return This builder instance
         */
        public Builder deviceInfo(Object deviceInfo) {
            this.deviceInfo = deviceInfo;
            return this;
        }

        /**
         * Sets the IP address
         *
         * @param ipAddress IP address string or object
         * @return This builder instance
         */
        public Builder ipAddress(Object ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        /**
         * Sets the user agent
         *
         * @param userAgent User agent string or object
         * @return This builder instance
         */
        public Builder userAgent(Object userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        /**
         * Sets the transaction ID
         *
         * @param transactionId Transaction identifier
         * @return This builder instance
         */
        public Builder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        /**
         * Sets the transaction timestamp
         *
         * @param transactionTimestamp Timestamp in ISO-8601 format
         * @return This builder instance
         */
        public Builder transactionTimestamp(String transactionTimestamp) {
            this.transactionTimestamp = transactionTimestamp;
            return this;
        }

        /**
         * Sets the currency code
         *
         * @param currencyCode ISO 4217 currency code
         * @return This builder instance
         */
        public Builder currencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
            return this;
        }

        /**
         * Sets the payment method
         *
         * @param paymentMethod Payment method identifier
         * @return This builder instance
         */
        public Builder paymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        /**
         * Sets the beneficiary information
         *
         * @param beneficiaryInfo Beneficiary details
         * @return This builder instance
         */
        public Builder beneficiaryInfo(BeneficiaryInfo beneficiaryInfo) {
            this.beneficiaryInfo = beneficiaryInfo;
            return this;
        }

        /**
         * Sets the customer tenure in days
         *
         * @param customerTenureDays Number of days the customer has had an account
         * @return This builder instance
         */
        public Builder customerTenureDays(int customerTenureDays) {
            this.customerTenureDays = customerTenureDays;
            return this;
        }

        /**
         * Adds an additional attribute
         *
         * @param key Attribute name
         * @param value Attribute value
         * @return This builder instance
         */
        public Builder addAttribute(String key, Object value) {
            this.additionalAttributes.put(key, value);
            return this;
        }

        /**
         * Adds multiple additional attributes
         *
         * @param attributes Map of attribute name-value pairs
         * @return This builder instance
         */
        public Builder addAttributes(Map<String, Object> attributes) {
            this.additionalAttributes.putAll(attributes);
            return this;
        }

        /**
         * Builds a new RiskAssessmentRequest
         *
         * @return New request instance
         * @throws IllegalArgumentException if required fields are missing
         */
        public RiskAssessmentRequest build() {
            // Validate required fields
            if (accountId == null) {
                throw new IllegalArgumentException("Account ID is required");
            }
            if (transactionAmount <= 0) {
                throw new IllegalArgumentException("Transaction amount must be positive");
            }
            if (transactionType == null || transactionType.isEmpty()) {
                throw new IllegalArgumentException("Transaction type is required");
            }

            // Set default timestamp if not provided
            if (transactionTimestamp == null) {
                this.transactionTimestamp = Instant.now().toString();
            }

            // Generate transaction ID if not provided
            if (transactionId == null) {
                this.transactionId = UUID.randomUUID().toString();
            }

            return new RiskAssessmentRequest(this);
        }
    }

    /**
     * Inner class representing beneficiary information
     */
    public static class BeneficiaryInfo {
        private final String accountNumber;
        private final String name;
        private final String bankCode;
        private final String country;
        private final boolean isNewBeneficiary;

        private BeneficiaryInfo(String accountNumber, String name, String bankCode,
                                String country, boolean isNewBeneficiary) {
            this.accountNumber = accountNumber;
            this.name = name;
            this.bankCode = bankCode;
            this.country = country;
            this.isNewBeneficiary = isNewBeneficiary;
        }

        public String getAccountNumber() {
            return accountNumber;
        }

        public String getName() {
            return name;
        }

        public String getBankCode() {
            return bankCode;
        }

        public String getCountry() {
            return country;
        }

        public boolean isNewBeneficiary() {
            return isNewBeneficiary;
        }

        /**
         * Creates a new builder for BeneficiaryInfo
         *
         * @return A new builder instance
         */
        public static BeneficiaryInfoBuilder builder() {
            return new BeneficiaryInfoBuilder();
        }

        /**
         * Builder for BeneficiaryInfo
         */
        public static class BeneficiaryInfoBuilder {
            private String accountNumber;
            private String name;
            private String bankCode;
            private String country;
            private boolean isNewBeneficiary;

            public BeneficiaryInfoBuilder accountNumber(String accountNumber) {
                this.accountNumber = accountNumber;
                return this;
            }

            public BeneficiaryInfoBuilder name(String name) {
                this.name = name;
                return this;
            }

            public BeneficiaryInfoBuilder bankCode(String bankCode) {
                this.bankCode = bankCode;
                return this;
            }

            public BeneficiaryInfoBuilder country(String country) {
                this.country = country;
                return this;
            }

            public BeneficiaryInfoBuilder isNewBeneficiary(boolean isNewBeneficiary) {
                this.isNewBeneficiary = isNewBeneficiary;
                return this;
            }

            public BeneficiaryInfo build() {
                return new BeneficiaryInfo(accountNumber, name, bankCode, country, isNewBeneficiary);
            }
        }
    }

    @Override
    public String toString() {
        return "RiskAssessmentRequest{" +
                "accountId=" + accountId +
                ", transactionAmount=" + transactionAmount +
                ", transactionType='" + transactionType + '\'' +
                ", location='" + location + '\'' +
                ", transactionId='" + transactionId + '\'' +
                '}';
    }
}
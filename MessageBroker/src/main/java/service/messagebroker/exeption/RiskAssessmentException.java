package service.messagebroker.exeption;

/**
 * Custom exception for risk assessment operations.
 * Thrown when risk assessment processing encounters errors.
 */
public class RiskAssessmentException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Error code to indicate the specific type of failure
     */
    private final String errorCode;

    /**
     * Transaction ID associated with the error, if available
     */
    private final String transactionId;

    /**
     * Creates a new RiskAssessmentException with a message
     *
     * @param message Error message describing the exception
     */
    public RiskAssessmentException(String message) {
        super(message);
        this.errorCode = "RISK_ASSESSMENT_ERROR";
        this.transactionId = null;
    }

    /**
     * Creates a new RiskAssessmentException with a message and cause
     *
     * @param message Error message describing the exception
     * @param cause Original exception that triggered this exception
     */
    public RiskAssessmentException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "RISK_ASSESSMENT_ERROR";
        this.transactionId = null;
    }

    /**
     * Creates a new RiskAssessmentException with a message, error code, and transaction ID
     *
     * @param message Error message describing the exception
     * @param errorCode Specific error code for the exception
     * @param transactionId ID of the transaction causing the exception
     */
    public RiskAssessmentException(String message, String errorCode, String transactionId) {
        super(message);
        this.errorCode = errorCode;
        this.transactionId = transactionId;
    }

    /**
     * Creates a new RiskAssessmentException with a message, cause, error code, and transaction ID
     *
     * @param message Error message describing the exception
     * @param cause Original exception that triggered this exception
     * @param errorCode Specific error code for the exception
     * @param transactionId ID of the transaction causing the exception
     */
    public RiskAssessmentException(String message, Throwable cause, String errorCode, String transactionId) {
        super(message, cause);
        this.errorCode = errorCode;
        this.transactionId = transactionId;
    }

    /**
     * Gets the error code associated with this exception
     *
     * @return Error code string
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Gets the transaction ID associated with this exception
     *
     * @return Transaction ID string or null if not available
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * Creates a new builder for RiskAssessmentException
     *
     * @return A new RiskAssessmentExceptionBuilder
     */
    public static RiskAssessmentExceptionBuilder builder() {
        return new RiskAssessmentExceptionBuilder();
    }

    /**
     * Builder class for creating RiskAssessmentException instances
     */
    public static class RiskAssessmentExceptionBuilder {
        private String message;
        private Throwable cause;
        private String errorCode = "RISK_ASSESSMENT_ERROR";
        private String transactionId;

        /**
         * Sets the exception message
         *
         * @param message Error message
         * @return This builder instance
         */
        public RiskAssessmentExceptionBuilder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * Sets the cause of the exception
         *
         * @param cause Original exception
         * @return This builder instance
         */
        public RiskAssessmentExceptionBuilder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        /**
         * Sets the error code
         *
         * @param errorCode Specific error code
         * @return This builder instance
         */
        public RiskAssessmentExceptionBuilder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        /**
         * Sets the transaction ID
         *
         * @param transactionId ID of the transaction
         * @return This builder instance
         */
        public RiskAssessmentExceptionBuilder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        /**
         * Builds a new RiskAssessmentException
         *
         * @return New exception instance
         */
        public RiskAssessmentException build() {
            if (cause == null) {
                return new RiskAssessmentException(message, errorCode, transactionId);
            } else {
                return new RiskAssessmentException(message, cause, errorCode, transactionId);
            }
        }
    }
}
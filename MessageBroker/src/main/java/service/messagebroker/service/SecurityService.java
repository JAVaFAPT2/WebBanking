package service.messagebroker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Service for handling security-related operations in the message broker
 * Provides input sanitization, validation, and other security utilities
 */
@Service
public class SecurityService {
    private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);

    // Pattern for basic input sanitization
    private static final Pattern UNSAFE_PATTERN = Pattern.compile("[<>\"'%;()&+]");

    /**
     * Sanitizes input strings to prevent injection attacks
     *
     * @param input The input string to sanitize
     * @return A sanitized version of the input string
     */
    public String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }

        // Log suspicious inputs for security monitoring
        if (UNSAFE_PATTERN.matcher(input).find()) {
            logger.warn("Potentially unsafe input detected: {}", input);
        }

        // Replace potentially dangerous characters
        String sanitized = UNSAFE_PATTERN.matcher(input).replaceAll("");

        // Trim and normalize whitespace
        sanitized = sanitized.trim().replaceAll("\\s+", " ");

        return sanitized;
    }

    /**
     * Validates if a string contains only alphanumeric characters and allowed symbols
     *
     * @param input The input string to validate
     * @return True if the input is valid, false otherwise
     */
    public boolean isValidInput(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        // Only allow alphanumeric characters, spaces, and some basic punctuation
        return input.matches("^[a-zA-Z0-9\\s.,_\\-:]+$");
    }

    /**
     * Validates if an input is safe for database operations
     * Helps prevent SQL injection attacks
     *
     * @param input The input to validate
     * @return True if the input is safe, false otherwise
     */
    public boolean isSafeForDatabase(String input) {
        if (input == null) {
            return true; // Null is safe, though it might cause other issues
        }

        // Check for common SQL injection patterns
        String lowerInput = input.toLowerCase();
        return !(lowerInput.contains("select ") ||
                lowerInput.contains("insert ") ||
                lowerInput.contains("update ") ||
                lowerInput.contains("delete ") ||
                lowerInput.contains("drop ") ||
                lowerInput.contains("union ") ||
                lowerInput.contains(";") ||
                lowerInput.contains("--") ||
                lowerInput.contains("/*") ||
                lowerInput.contains("*/"));
    }

    /**
     * Encodes a string to prevent XSS attacks
     *
     * @param input The input string to encode
     * @return An encoded version of the input string
     */
    public String encodeForHtml(String input) {
        if (input == null) {
            return null;
        }

        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    /**
     * Validates if an IP address is from a trusted network
     *
     * @param ipAddress The IP address to validate
     * @return True if the IP is trusted, false otherwise
     */
    public boolean isTrustedIpAddress(String ipAddress) {
        // This would typically check against a whitelist or CIDR ranges
        // For this example, we'll just do a simple check
        if (ipAddress == null || ipAddress.isEmpty()) {
            return false;
        }

        // Example: Check if IP is internal
        return ipAddress.startsWith("10.") ||
                ipAddress.startsWith("192.168.") ||
                ipAddress.startsWith("172.16.") ||
                ipAddress.equals("127.0.0.1");
    }

    /**
     * Logs a security event for audit purposes
     *
     * @param eventType The type of security event
     * @param details Details about the security event
     * @param severity The severity level of the event
     */
    public void logSecurityEvent(String eventType, String details, String severity) {
        switch (severity.toUpperCase()) {
            case "HIGH":
                logger.error("SECURITY EVENT [{}]: {}", eventType, details);
                break;
            case "MEDIUM":
                logger.warn("SECURITY EVENT [{}]: {}", eventType, details);
                break;
            case "LOW":
                logger.info("SECURITY EVENT [{}]: {}", eventType, details);
                break;
            default:
                logger.debug("SECURITY EVENT [{}]: {}", eventType, details);
        }

        // In a real implementation, this would also send the event to a security monitoring system
    }
}

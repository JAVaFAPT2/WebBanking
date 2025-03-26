package service.shared.util;



import service.shared.exception.BankingException;
import service.shared.models.Account;
import service.shared.models.Transaction;
import service.shared.models.Transfer;
import service.shared.models.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class BankingUtils {

    private BankingUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Generates a random UUID for entities.
     *
     * @return A randomly generated UUID
     */
    public static UUID generateId() {
        return UUID.randomUUID();
    }

    /**
     * Validates if an account has sufficient balance for a transaction.
     *
     * @param account The account to check
     * @param amount The amount to check against
     * @return true if balance is sufficient, false otherwise
     */
    public static boolean hasSufficientBalance(Account account, BigDecimal amount) {
        return account.getBalance().compareTo(amount) >= 0;
    }

    /**
     * Logs a transaction with relevant details.
     *
     * @param transaction The transaction to log
     */
    public static void logTransaction(Transaction transaction) {
        String logMessage = String.format(
                "Transaction ID: %s, Type: %s, Amount: %s, Account: %s, Timestamp: %s",
                transaction.getId(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getAccount().getId(),
                transaction.getCreatedDate()
        );
        System.out.println(logMessage); // In production, use a proper logging framework
    }

    /**
     * Logs a transfer with relevant details.
     *
     * @param transfer The transfer to log
     */
    public static void logTransfer(Transfer transfer) {
        String logMessage = String.format(
                "Transfer ID: %s, Amount: %s, From Account: %s, To Account: %s, Timestamp: %s",
                transfer.getId(),
                transfer.getAmount(),
                transfer.getFromAccount().getId(),
                transfer.getToAccount().getId(),
                transfer.getCreatedDate()
        );
        System.out.println(logMessage); // In production, use a proper logging framework
    }

    /**
     * Formats a LocalDateTime object to a string in ISO format.
     *
     * @param dateTime The LocalDateTime to format
     * @return Formatted date time string
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        return dateTime.format(formatter);
    }

    /**
     * Parses a date time string to LocalDateTime object.
     *
     * @param dateTimeString The date time string to parse
     * @return Parsed LocalDateTime object
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        return LocalDateTime.parse(dateTimeString, formatter);
    }

    /**
     * Validates if a user is authorized to perform an operation on an account.
     *
     * @param user The user to check
     * @param account The account to check
     * @return true if authorized, false otherwise
     */
    public static boolean isUserAuthorized(User user, Account account) {
        return account.getUser().getId().equals(user.getId());
    }

    /**
     * Validates if a transfer is valid between two accounts.
     *
     * @param transfer The transfer to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidTransfer(Transfer transfer) {
        return transfer.getFromAccount() != null &&
                transfer.getToAccount() != null &&
                transfer.getAmount().compareTo(BigDecimal.ZERO) > 0 &&
                transfer.getFromAccount().getId() != transfer.getToAccount().getId();
    }

    /**
     * Generates a random transaction reference number.
     *
     * @return A random reference number
     */
    public static String generateReferenceNumber() {
        return String.format("REF-%08d", ThreadLocalRandom.current().nextInt(100000000));
    }

    /**
     * Validates if a user has a specific role.
     *
     * @param user The user to check
     * @param role The role to check for
     * @return true if user has the role, false otherwise
     */
    public static boolean hasRole(User user, String role) {
        return user.getRole().equalsIgnoreCase(role);
    }

    /**
     * Converts an amount from one currency to another.
     * (This is a simplified version and would integrate with an actual exchange service in production)
     *
     * @param amount The amount to convert
     * @param fromCurrency The source currency
     * @param toCurrency The target currency
     * @return Converted amount
     */
    public static BigDecimal convertCurrency(BigDecimal amount, String fromCurrency, String toCurrency) {
        // In production, this would call an external exchange rate API
        // For demonstration purposes, we'll use a fixed rate
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return amount;
        }

        // Example fixed rates (USD to other currencies)
        Map<String, Double> exchangeRates = Map.of(
                "USD", 1.0,
                "EUR", 0.85,
                "GBP", 0.73,
                "JPY", 110.0
        );

        Double fromRate = exchangeRates.getOrDefault(fromCurrency, 1.0);
        Double toRate = exchangeRates.getOrDefault(toCurrency, 1.0);

        return amount.multiply(BigDecimal.valueOf(fromRate / toRate)).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Validates required fields for a banking operation.
     *
     * @param data The data to validate
     * @param requiredFields The required fields
     * @throws BankingException if any required field is missing
     */
    public static void validateRequiredFields(Map<String, Object> data, String... requiredFields) throws BankingException {
        for (String field : requiredFields) {
            if (!data.containsKey(field) || data.get(field) == null) {
                throw new BankingException("Missing required field: " + field);
            }
        }
    }
}

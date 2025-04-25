package service.messagebroker.service.impl;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import service.messagebroker.service.transactionHistoryService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Service for accessing and analyzing transaction history
 * Provides methods to query transaction patterns and history for risk assessment
 */
@Service
public class transactionHistoryServiceImpl implements transactionHistoryService {
    private static final Logger logger = LoggerFactory.getLogger(transactionHistoryServiceImpl.class);

    private final JdbcTemplate jdbcTemplate;

    // Cache to reduce database load for frequent queries
    private final Map<String, CachedTransactionCount> transactionCountCache = new ConcurrentHashMap<>();

    // Cache expiration time in milliseconds
    @Value("${app.transaction.cache.expiry-ms:60000}")
    private long cacheExpiryMs;

    // Maximum cache size to prevent memory issues
    @Value("${app.transaction.cache.max-size:10000}")
    private int maxCacheSize;

    public transactionHistoryServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Gets the count of recent transactions for an account within a specified time window
     *
     * @param accountId The account ID to check
     * @param timeWindowMinutes The time window in minutes
     * @return The number of transactions in the specified window
     */
    public Integer getRecentTransactionCount(UUID accountId, int timeWindowMinutes) {
        if (accountId == null) {
            logger.warn("Null accountId provided to getRecentTransactionCount");
            return 0;
        }

        String cacheKey = accountId + ":" + timeWindowMinutes;

        // Check cache first
        CachedTransactionCount cachedCount = transactionCountCache.get(cacheKey);
        if (cachedCount != null && !cachedCount.isExpired()) {
            logger.debug("Cache hit for transaction count: {}", cacheKey);
            return cachedCount.getCount();
        }

        // Cache miss, query the database
        logger.debug("Cache miss for transaction count: {}", cacheKey);

        try {
            String sql = "SELECT COUNT(*) FROM transactions WHERE account_id = ? AND " +
                    "transaction_time > NOW() - INTERVAL ? MINUTE";

            Integer count = jdbcTemplate.queryForObject(
                    sql,
                    Integer.class,
                    accountId.toString(), timeWindowMinutes);

            // Store in cache
            if (count != null) {
                // Manage cache size
                if (transactionCountCache.size() >= maxCacheSize) {
                    // Remove a random entry if cache is full
                    String keyToRemove = transactionCountCache.keySet().iterator().next();
                    transactionCountCache.remove(keyToRemove);
                }

                transactionCountCache.put(cacheKey, new CachedTransactionCount(count));
                return count;
            }

            return 0;
        } catch (Exception e) {
            logger.error("Error querying transaction count for account {}: {}", accountId, e.getMessage());
            return 0; // Return 0 as a safe default
        }
    }

    /**
     * Gets the average transaction amount for an account over the last N days
     *
     * @param accountId The account ID
     * @param days Number of days to look back
     * @return The average transaction amount or 0 if no transactions
     */
    public double getAverageTransactionAmount(UUID accountId, int days) {
        try {
            String sql = "SELECT AVG(amount) FROM transactions WHERE account_id = ? AND " +
                    "transaction_time > NOW() - INTERVAL ? DAY";

            Double average = jdbcTemplate.queryForObject(
                    sql,
                    Double.class,
                    accountId.toString(), days);

            return average != null ? average : 0.0;
        } catch (Exception e) {
            logger.error("Error querying average transaction amount for account {}: {}", accountId, e.getMessage());
            return 0.0;
        }
    }

    /**
     * Checks if this is the first transaction with a specific recipient
     *
     * @param accountId The sender account ID
     * @param recipientId The recipient account ID
     * @return True if this is a new recipient, false otherwise
     */
    public boolean isNewRecipient(UUID accountId, String recipientId) {
        try {
            String sql = "SELECT COUNT(*) FROM transactions WHERE account_id = ? AND recipient_id = ?";

            Integer count = jdbcTemplate.queryForObject(
                    sql,
                    Integer.class,
                    accountId.toString(), recipientId);

            return count == null || count == 0;
        } catch (Exception e) {
            logger.error("Error checking if recipient is new for account {}: {}", accountId, e.getMessage());
            return true; // Assume new recipient if we can't verify (more cautious)
        }
    }

    /**
     * Gets the time since the last transaction for an account in seconds
     *
     * @param accountId The account ID
     * @return Time in seconds since last transaction, or a large value if no previous transactions
     */
    public double getTimeSinceLastTransaction(UUID accountId) {
        try {
            String sql = "SELECT MAX(transaction_time) FROM transactions WHERE account_id = ?";

            LocalDateTime lastTransactionTime = jdbcTemplate.queryForObject(
                    sql,
                    LocalDateTime.class,
                    accountId.toString());

            if (lastTransactionTime == null) {
                return Double.MAX_VALUE; // No previous transactions
            }

            LocalDateTime now = LocalDateTime.now();
            return java.time.Duration.between(lastTransactionTime, now).getSeconds();
        } catch (Exception e) {
            logger.error("Error getting time since last transaction for account {}: {}", accountId, e.getMessage());
            return Double.MAX_VALUE; // Return a large value as a safe default
        }
    }

    /**
     * Gets a map of transaction locations and their frequencies for an account
     *
     * @param accountId The account ID
     * @param days Number of days to look back
     * @return Map of locations to their frequency counts
     */
    public Map<String, Integer> getTransactionLocationFrequency(UUID accountId, int days) {
        try {
            String sql = "SELECT location, COUNT(*) as frequency FROM transactions " +
                    "WHERE account_id = ? AND transaction_time > NOW() - INTERVAL ? DAY " +
                    "GROUP BY location ORDER BY frequency DESC";

            return jdbcTemplate.query(
                    sql,
                    rs -> {
                        Map<String, Integer> result = new HashMap<>();
                        while (rs.next()) {
                            result.put(rs.getString("location"), rs.getInt("frequency"));
                        }
                        return result;
                    },
                    accountId.toString(), days);
        } catch (Exception e) {
            logger.error("Error getting location frequency for account {}: {}", accountId, e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Clears the transaction count cache
     */
    public void clearCache() {
        transactionCountCache.clear();
        logger.info("Transaction count cache cleared");
    }

    /**
     * Inner class to store cached transaction counts with expiration
     */
    private class CachedTransactionCount {
        @Getter
        private final int count;
        private final long timestamp;

        public CachedTransactionCount(int count) {
            this.count = count;
            this.timestamp = System.currentTimeMillis();
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > cacheExpiryMs;
        }
    }
}

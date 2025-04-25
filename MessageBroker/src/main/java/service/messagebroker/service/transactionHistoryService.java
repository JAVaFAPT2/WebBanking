package service.messagebroker.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;


public interface transactionHistoryService {
    Integer getRecentTransactionCount(UUID accountId, int timeWindowMinutes);
    double getAverageTransactionAmount(UUID accountId, int days);
    boolean isNewRecipient(UUID accountId, String recipientId);
    double getTimeSinceLastTransaction(UUID accountId);
    Map<String, Integer> getTransactionLocationFrequency(UUID accountId, int days);
    void clearCache();
}

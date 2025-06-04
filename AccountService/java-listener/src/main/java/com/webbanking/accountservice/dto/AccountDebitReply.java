package com.webbanking.accountservice.dto;

import java.math.BigDecimal;

// This DTO is sent back to OrchestrationService
public record AccountDebitReply(
        String sagaId,
        String globalTransactionId,
        String accountId,
        boolean success,
        String failureReason, // Null if success
        BigDecimal newBalance // Optional: could be useful for logging or other purposes
) {
} 
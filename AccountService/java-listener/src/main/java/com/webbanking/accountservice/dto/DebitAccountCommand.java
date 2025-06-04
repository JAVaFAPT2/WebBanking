package com.webbanking.accountservice.dto;

import java.math.BigDecimal;

// This DTO should match the one sent by OrchestrationService
public record DebitAccountCommand(
        String sagaId,
        String globalTransactionId, // To correlate with the overall fund transfer
        String accountId, 
        BigDecimal amount,
        String currency
) {
} 
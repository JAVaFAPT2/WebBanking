package com.webbanking.transactionservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// This DTO is consumed by TransactionService listener.
// It should match the structure of the command sent by OrchestrationService.
public record RecordTransactionCommand(
        String sagaId,
        String transactionId,       // The unique ID for this transaction, often same as globalTransactionId or derived
        String sourceAccountId,
        String destinationAccountId,
        BigDecimal amount,
        String currency,
        String transactionType,     // e.g., "FUND_TRANSFER", "PAYMENT"
        String status,              // e.g., "PENDING", "COMPLETED_BY_ORCHESTRATOR"
        LocalDateTime initiatedAt,     // Timestamp from the orchestrator or originating service
        String description
) {
} 
package com.webbanking.transactionservice.dto;

import java.time.LocalDateTime;

// This DTO is sent back to OrchestrationService
public record TransactionRecordedReply(
        String sagaId,
        String transactionId, // The ID of the transaction that was recorded
        boolean success,
        String failureReason, // Null if success
        LocalDateTime recordedAt // Timestamp when the transaction was actually recorded by this service
) {
} 
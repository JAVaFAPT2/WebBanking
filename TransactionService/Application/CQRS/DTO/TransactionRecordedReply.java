package com.webbanking.transactionservice.application.cqrs.dto;

/**
 * Reply from TransactionService after attempting to record a transaction.
 */
public record TransactionRecordedReply(
        String sagaId,
        String transactionId, // ID of the transaction operation
        boolean success,
        String failureReason // Null if successful
) {
} 
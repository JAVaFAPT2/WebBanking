package com.webbanking.accountservice.application.cqrs.dto;

/**
 * Reply from AccountService after attempting to debit an account.
 */
public record DebitAccountReply(
        String sagaId,
        String transactionId, // ID of the debit operation
        boolean success,
        String failureReason // Null if successful
) {
} 
package com.webbanking.accountservice.application.cqrs.dto;

/**
 * Reply from AccountService after attempting to credit an account.
 */
public record CreditAccountReply(
        String sagaId,
        String transactionId, // ID of the credit operation
        boolean success,
        String failureReason // Null if successful
) {
} 
package com.webbanking.accountservice.application.cqrs.dto;

import java.math.BigDecimal;

/**
 * Command to debit an account as part of a fund transfer saga.
 */
public record DebitAccountCommand(
        String sagaId,
        String sourceAccountId,
        String destinationAccountId, // Included for context, though AccountService primarily cares about sourceAccountId for debit
        BigDecimal amount,
        String transactionId // ID for this specific debit operation
) {
} 
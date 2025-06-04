package com.webbanking.accountservice.application.cqrs.dto;

import java.math.BigDecimal;

/**
 * Command to credit an account as part of a fund transfer saga.
 */
public record CreditAccountCommand(
        String sagaId,
        String destinationAccountId,
        BigDecimal amount,
        String transactionId // ID for this specific credit operation
) {
} 
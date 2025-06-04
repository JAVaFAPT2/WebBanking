package com.webbanking.accountservice.application.cqrs.dto;

import java.math.BigDecimal;

/**
 * Command to instruct the AccountService to compensate a previously successful debit.
 */
public record CompensateDebitCommand(
        String sagaId,                     // ID of the saga initiating this compensation
        String originalDebitTransactionId, // ID of the original debit transaction to be compensated
        String accountId,                  // The account to be credited (compensated)
        BigDecimal amount,                 // The amount to credit back
        String reason                      // Reason for compensation
) {
} 
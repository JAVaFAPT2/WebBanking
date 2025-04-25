package service.messagebroker.DTo;

import java.util.UUID;

public record TransactionEventDTO(UUID accountId,
                                  String transactionId,
                                  double amount,
                                  String description,
                                  boolean isInternational) {
    // No additional methods or fields needed
}

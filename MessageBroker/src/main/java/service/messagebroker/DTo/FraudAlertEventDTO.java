package service.messagebroker.DTo;

import java.util.Map;

public record FraudAlertEventDTO(String accountId,
                                 String transactionId,
                                 String fraudType,
                                 Map<String, Object> details) {
}

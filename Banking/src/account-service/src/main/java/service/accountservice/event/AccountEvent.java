package service.accountservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import service.shared.models.Account;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor@Data
public class AccountEvent {
    private UUID accountId;
    private String eventType;
    private Account account;
    private Instant timestamp;
    private Map<String, Object> metadata;
}

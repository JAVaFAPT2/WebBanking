package service.shared.event;

import lombok.*;
import service.shared.models.Account;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
public class AccountEvent {
    private UUID accountId;
    private String eventType;
    private UUID eventId;
    private Account account;
    private Instant timestamp;
    private Map<String, Object> metadata;

    public <K, V> AccountEvent(UUID id, String accountCreated, Account accountObject, LocalDateTime now, Map<String,Object> commandType) {
        this.accountId = id;
        this.eventType = accountCreated;
        this.eventId = UUID.randomUUID();
        this.account = accountObject;
        this.timestamp = Instant.now();
        this.metadata = commandType;
    }
}


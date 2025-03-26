package service.shared.event;

import lombok.*;
import service.shared.models.Account;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AccountEvent {
    private UUID eventId;
    private String eventType;
    private Account account;
    private LocalDateTime timestamp;
    private Map<String, Object> data;
}

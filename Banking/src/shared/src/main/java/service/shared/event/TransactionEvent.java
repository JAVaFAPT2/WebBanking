package service.shared.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import service.shared.models.Transaction;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionEvent {
    private UUID eventId;
    private String eventType;
    private Transaction transaction;
    private LocalDateTime timestamp;
    private Map<String, Object> data;
}

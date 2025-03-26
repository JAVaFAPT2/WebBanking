package service.shared.event;

import lombok.*;
import service.shared.models.Transfer;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TransferEvent {
    private UUID eventId;
    private String eventType;
    private Transfer transfer;
    private LocalDateTime timestamp;
    private Map<String, Object> data;
}

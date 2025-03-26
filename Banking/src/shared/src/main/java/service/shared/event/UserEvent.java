package service.shared.event;

import lombok.*;
import service.shared.models.User;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserEvent {
    private UUID eventId;
    private String eventType;
    private User user;
    private LocalDateTime timestamp;
    private Map<String, Object> data;
}

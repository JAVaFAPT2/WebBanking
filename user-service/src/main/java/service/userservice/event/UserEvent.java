package service.userservice.event;

import lombok.*;
import service.shared.models.BaseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEvent extends BaseEntity {
    private String eventType;
    private UUID userId;
    private Instant timestamp = Instant.now();
}

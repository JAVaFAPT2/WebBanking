package service.shared.event;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing an audit event.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AuditEvent {

    @Id
    private UUID id ;

    @Column(nullable = false)
    private String level;

    @Column(nullable = false, length = 1024)
    private String message;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column
    private String serviceName;
}
package service.shared.event;

import jakarta.persistence.*;
import lombok.*;
import service.auditservice.event.listener.AuditEventListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing an audit event.
 */
@Entity
@Table(name = "audit_events")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EntityListeners(AuditEventListener.class)
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
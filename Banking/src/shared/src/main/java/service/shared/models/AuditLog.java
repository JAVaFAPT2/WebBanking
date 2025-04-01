package service.shared.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;





@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue
    private UUID id = UUID.randomUUID();

    // A brief message describing the event.
    @Column(nullable = false)
    private String message;

    // Any additional details you want to record (could be JSON).
    @Column(length = 2048)
    private String details;

    // The time when the event was recorded.
    @Column(nullable = false)
    private LocalDateTime timestamp;

    public AuditLog(String message, Object details) {
        this.message = message;
        this.details = details.toString();
        this.timestamp = LocalDateTime.now();
    }
}

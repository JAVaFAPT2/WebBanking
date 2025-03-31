package service.shared.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1024)
    private String message;

    @Column(nullable = false)
    private String recipientId; // User ID of the recipient

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;
}

package service.shared.event;


import lombok.*;
import org.springframework.context.ApplicationEvent;
import java.time.LocalDateTime;
import java.util.UUID;


/**
 * Entity representing an audit event.
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class AuditEvent extends ApplicationEvent {
    private final UUID id = UUID.randomUUID();
    private final String message;
    private final String details;
    protected LocalDateTime eventTimestamp;


    public AuditEvent(Object source, String message, String details) {
        super(source);
        this.message = message;
        this.details = details;
        this.eventTimestamp = LocalDateTime.now();
    }


}
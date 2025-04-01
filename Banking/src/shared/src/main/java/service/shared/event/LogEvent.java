package service.shared.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class LogEvent extends ApplicationEvent {
    private final String logMessage;
    public LogEvent(Object source, String logMessage) {
        super(source);
        this.logMessage = logMessage;
    }
}

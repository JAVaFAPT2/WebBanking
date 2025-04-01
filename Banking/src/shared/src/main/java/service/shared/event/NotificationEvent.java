package service.shared.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
import service.shared.models.Notification;

@Getter
@Setter
public class NotificationEvent extends ApplicationEvent {
    private final Notification notification;
    public NotificationEvent(Object source, Notification notification) {
        super(source);
        this.notification = notification;
    }

}

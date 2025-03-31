package service.shared.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import service.shared.models.Notification;

@Getter
public class NotificationEvent extends ApplicationEvent {
    private final Notification notification;
    public NotificationEvent(Object source, Notification notification) {
        super(source);
        this.notification = notification;
    }

}

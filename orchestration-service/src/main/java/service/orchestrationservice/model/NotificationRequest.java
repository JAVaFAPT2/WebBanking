package service.orchestrationservice.model;

import lombok.*;


import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationRequest {
    private UUID userId;
    private String message;
    private String type;

    public NotificationRequest(UUID userId, String s) {
        this.userId = userId;
        this.message = s;
    }
}

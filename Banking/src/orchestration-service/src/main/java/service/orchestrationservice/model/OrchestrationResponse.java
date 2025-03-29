package service.orchestrationservice.model;

import lombok.*;
import service.shared.event.OrchestrationStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrchestrationResponse {
    private OrchestrationStatus status;
    private String message;
    private Object data;
}

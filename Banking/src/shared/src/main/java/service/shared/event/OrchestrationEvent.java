package service.shared.event;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrchestrationEvent {
    private String orchestrationId;
    private UUID userId;
    private UUID fromAccountId;
    private UUID toAccountId;
    private double amount;
    private OrchestrationStatus status;
    private String message;

    public OrchestrationEvent(String orchestrationId, OrchestrationStatus orchestrationStatus, String transferCompletedSuccessfully) {
            this.orchestrationId = orchestrationId;
            this.status = orchestrationStatus;
            this.message = transferCompletedSuccessfully;

    }
    public OrchestrationEvent(String orchestrationId, UUID fromAccountId, UUID toAccountId, double amount) {
        this.orchestrationId = orchestrationId;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.status = OrchestrationStatus.INITIATED;
    }



}

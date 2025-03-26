package service.orchestrationservice.model;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TransferRequest {
    private UUID fromAccountId;
    private UUID toAccountId;
    private double amount;
    private String transfermsg;

    public TransferRequest(UUID fromAccountId, UUID toAccountId, double amount) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
    }
}
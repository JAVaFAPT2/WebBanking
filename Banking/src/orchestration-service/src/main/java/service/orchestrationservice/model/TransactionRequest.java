package service.orchestrationservice.model;

import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Data
public class TransactionRequest {
    private UUID userId;
    private UUID toAccountId;
    private UUID fromAccountId;
    private double amount;
    private String type;

}

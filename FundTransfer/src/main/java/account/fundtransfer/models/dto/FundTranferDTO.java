package account.fundtransfer.models.dto;

import account.fundtransfer.models.TranferType;
import account.fundtransfer.models.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FundTranferDTO {
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private String transactionReference;
    private TransactionStatus status;
    private TranferType tranferType;
    private LocalDateTime transactionDate;
}

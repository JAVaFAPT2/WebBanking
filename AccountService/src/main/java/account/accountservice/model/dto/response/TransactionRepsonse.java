package account.accountservice.model.dto.response;

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
public class TransactionRepsonse {

    private String accountId;
    private String referentId;
    private String transactionType;
    private String transactionStatus;
    private BigDecimal amount;
    private LocalDateTime transactionTime;
    private String comments;

}


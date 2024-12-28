package test.projectcv.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import test.projectcv.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private Long id;
    private Long accountId;
    private BigDecimal amount;
    private LocalDateTime transactionDate;
    private Transaction.TransactionType transactionType;
}
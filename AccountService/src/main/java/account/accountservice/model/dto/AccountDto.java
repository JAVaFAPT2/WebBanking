package account.accountservice.model.dto;


import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class AccountDto {
    private Long accountId;
    private String accountNunber;
    private String accountType;
    private String accountStatus;
    private BigDecimal availableBalance;
    private Long UserId;

}

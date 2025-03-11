package account.accountservice.model.dto;

import account.accountservice.model.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
public class AccountStatusUpdate {
    AccountStatus accountStatus;
}

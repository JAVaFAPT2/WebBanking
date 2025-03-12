package org.account.service.model.dto;

import lombok.Data;
import org.account.service.model.AccountStatus;

@Data
public class AccountStatusUpdate {
    AccountStatus accountStatus;
}

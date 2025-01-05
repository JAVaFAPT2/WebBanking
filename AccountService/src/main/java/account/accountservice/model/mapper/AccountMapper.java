package account.accountservice.model.mapper;

import account.accountservice.model.dto.AccountDto;
import account.accountservice.model.entity.Account;
import org.springframework.beans.BeanUtils;

import java.util.Objects;

public class AccountMapper extends BaseMapper<Account, AccountDto> {
    @Override
    public Account convertToEntity(AccountDto dto, Object... args) {
        Account account = new Account();
        if(!Objects.isNull(dto)){
            BeanUtils.copyProperties(dto, account);
        }
        return account;
    }

    @Override
    public AccountDto convertToDto(Account entity, Object... args) {
        AccountDto accountDto = new AccountDto();
        BeanUtils.copyProperties(entity, accountDto);
        return accountDto;
    }
}

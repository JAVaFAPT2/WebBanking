package test.projectcv.service;
import test.projectcv.dto.AccountDto;
import java.util.List;

public interface AccountService {
    AccountDto createAccount(AccountDto accountDto);
    AccountDto getAccount(Long accountId);
    List<AccountDto> getAllAccounts();
    AccountDto updateAccount(AccountDto accountDto);
    void deleteAccount(Long accountId);
}

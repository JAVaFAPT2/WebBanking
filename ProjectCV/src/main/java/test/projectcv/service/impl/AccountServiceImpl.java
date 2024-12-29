package test.projectcv.service.impl;

import org.springframework.stereotype.Service;
import test.projectcv.dto.AccountDto;
import test.projectcv.service.AccountService;

import java.util.List;
@Service
public class AccountServiceImpl implements AccountService {
    @Override
    public AccountDto createAccount(AccountDto accountDto) {
        return null;
    }

    @Override
    public AccountDto getAccount(Long accountId) {
        return null;
    }

    @Override
    public List<AccountDto> getAllAccounts() {
        return List.of();
    }

    @Override
    public AccountDto updateAccount(AccountDto accountDto) {
        return null;
    }

    @Override
    public void deleteAccount(Long accountId) {

    }
}

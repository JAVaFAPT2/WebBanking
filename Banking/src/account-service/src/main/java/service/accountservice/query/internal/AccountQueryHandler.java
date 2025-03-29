package service.accountservice.query.internal;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;
import service.accountservice.query.api.GetAccountQuery;
import service.accountservice.command.internal.model.AccountWriteModel;
import service.accountservice.query.internal.model.AccountReadModel;
import service.accountservice.repository.AccountReadRepository;
import service.accountservice.repository.AccountWriteRepository;

@Service
public class AccountQueryHandler {

    private final AccountReadRepository accountReadRepository;

    public AccountQueryHandler(AccountReadRepository _accountReadRepository) {
        this.accountReadRepository = _accountReadRepository;
    }

    public Mono<AccountReadModel> handleGetAccount(GetAccountQuery query) {
        return Mono.justOrEmpty(accountReadRepository.findById(query.getAccountId()))
                .switchIfEmpty(Mono.error(new RuntimeException("Account not found with id: " + query.getAccountId())));
    }
}

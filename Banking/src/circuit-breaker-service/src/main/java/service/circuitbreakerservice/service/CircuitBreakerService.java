package service.circuitbreakerservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.circuitbreakerservice.DTo.AccountDto;
import service.circuitbreakerservice.DTo.UserDto;
import service.circuitbreakerservice.feign.AccountClient;
import service.circuitbreakerservice.feign.UserClient;

import java.util.UUID;

@Service
public class CircuitBreakerService {
    private final UserClient userClient;
    private final AccountClient accountClient;

    @Autowired
    public CircuitBreakerService(UserClient userClient, AccountClient accountClient) {
        this.userClient = userClient;
        this.accountClient = accountClient;
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "userFallback")
    public UserDto getUser(UUID userId) {
        return userClient.getUser(userId);
    }

    public String userFallback(String userId, Throwable throwable) {
            return "err" + userId + throwable;
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "accountFallback")
    public AccountDto getAccount(UUID accountId) {
        return accountClient.getAccount(accountId);
    }

    public String accountFallback(String accountId, Throwable throwable) {
        return "err" + accountId +":"+ throwable;
    }
}
package service.circuitbreakerservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.circuitbreakerservice.DTo.AccountDto;
import service.circuitbreakerservice.DTo.UserDto;
import service.circuitbreakerservice.service.CircuitBreakerService;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class CircuitBreakerController {
    private final CircuitBreakerService circuitBreakerService;

    @Autowired
    public CircuitBreakerController(CircuitBreakerService circuitBreakerService) {
        this.circuitBreakerService = circuitBreakerService;
    }


    @GetMapping("/api/circuit-breaker/users/{userId}")
    public UserDto getUser(@PathVariable UUID userId) {
        return circuitBreakerService.getUser(userId);
    }

    @GetMapping("/api/circuit-breaker/accounts/{accountId}")
    public AccountDto getAccount(@PathVariable UUID accountId) {
        return circuitBreakerService.getAccount(accountId);
    }
}

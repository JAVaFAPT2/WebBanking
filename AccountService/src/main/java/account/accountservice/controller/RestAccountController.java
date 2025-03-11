package account.accountservice.controller;

import account.accountservice.model.dto.AccountDto;
import account.accountservice.model.dto.AccountStatusUpdate;
import account.accountservice.model.dto.external.TransactionRepsonse;
import account.accountservice.model.dto.response.Response;
import account.accountservice.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("account")
@Slf4j
@RequiredArgsConstructor

public class RestAccountController {
    private final AccountService accountService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Response> createAccount(@Valid @RequestBody AccountDto accountDto) {
        log.info("Creating new account for user: {}", accountDto.getUserId());
        Response response = accountService.createAccount(accountDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping
    public ResponseEntity<Response> updateAccountStatus(
            @NotBlank @RequestParam String accountNumber,
            @Valid @RequestBody AccountStatusUpdate accountStatusUpdate) {
        log.info("Updating status for account: {}", accountNumber);
        Response response = accountService.updateStatus(accountNumber, accountStatusUpdate);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<AccountDto> getAccountByNumber(
            @NotBlank @RequestParam String accountNumber) {
        log.debug("Fetching account details for account: {}", accountNumber);
        AccountDto account = accountService.readAccountByAccountNumber(accountNumber);
        return ResponseEntity.ok(account);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> updateAccount(
            @NotBlank @RequestParam String accountNumber,
            @Valid @RequestBody AccountDto accountDto) {
        log.info("Updating account: {}", accountNumber);
        Response response = accountService.updateAccount(accountNumber, accountDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/balance")
    public ResponseEntity<String> getAccountBalance(
            @NotBlank @RequestParam String accountNumber) {
        log.debug("Fetching balance for account: {}", accountNumber);
        String balance = accountService.getBalance(accountNumber);
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/{accountId}/transactions")
    public ResponseEntity<List<TransactionRepsonse>> getAccountTransactions(
            @NotBlank @PathVariable String accountId) {
        log.debug("Fetching transactions for account ID: {}", accountId);
        List<TransactionRepsonse> transactions = accountService.getTransactionsFromAccountId(accountId);
        return ResponseEntity.ok(transactions);
    }

    @PutMapping("/closure")
    public ResponseEntity<Response> closeAccount(
            @NotBlank @RequestParam String accountNumber) {
        log.info("Closing account: {}", accountNumber);
        Response response = accountService.closeAccount(accountNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AccountDto> getAccountByUserId(
            @PathVariable Long userId) {
        log.debug("Fetching account for user ID: {}", userId);
        AccountDto account = accountService.readAccountByUserId(userId);
        return ResponseEntity.ok(account);
    }
}

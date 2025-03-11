package account.fundtransfer.external;

import account.fundtransfer.models.dto.Account;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.feign.FeignClientProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

@Service
@FeignClient(name = "account-service", configuration = FeignClientProperties.FeignClientConfiguration.class)
public interface AccountService
{
    @GetMapping("/accounts/{accountId}")
    ResponseEntity<Account> readAccountById (@PathVariable Long accountId);
    @GetMapping("/accounts")
    ResponseEntity<Account> readAccountByAccountNumber (@PathVariable String accountNumber);
    @PutMapping("/accounts/{accountNumber}")
    ResponseEntity<Account> updateAccount(@PathVariable String accountNumber, @RequestBody Account account);
}

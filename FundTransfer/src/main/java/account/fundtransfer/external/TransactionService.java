package account.fundtransfer.external;

import account.accountservice.model.dto.response.TransactionRepsonse;
import account.fundtransfer.models.dto.Transaction;
import account.fundtransfer.models.dto.response.Response;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.feign.FeignClientProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
@Service
@FeignClient(name = "account-service", configuration = FeignClientProperties.FeignClientConfiguration.class)
public interface TransactionService {
    @GetMapping("/transaction")
    List<TransactionRepsonse> getTransactionFormAccountId(String accountId);
    @PostMapping("/transaction/internal")
    ResponseEntity<Response> makeInternalTransaction(@RequestBody List<Transaction> transaction, @RequestParam String transactionReference);
}

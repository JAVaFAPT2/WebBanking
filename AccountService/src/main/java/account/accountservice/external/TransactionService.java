package account.accountservice.external;


import account.accountservice.model.dto.external.TransactionRepsonse;
import org.springframework.cloud.netflix.feign.FeignAutoConfiguration;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
@Service
@FeignClient(name = "transaction-service", configuration = FeignAutoConfiguration.class)
public interface TransactionService {
    @GetMapping("/transaction")
    List<TransactionRepsonse> getTransactionFormAccountId(String accountId);
}

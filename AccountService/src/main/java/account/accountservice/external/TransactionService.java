package account.accountservice.external;


import account.accountservice.model.dto.response.TransactionRepsonse;
import org.springframework.cloud.netflix.feign.FeignAutoConfiguration;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.feign.FeignClientProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
@Service
@FeignClient(name = "transaction-service", configuration = FeignClientProperties.FeignClientConfiguration.class)
public interface TransactionService {
    @GetMapping("/transaction")
    List<TransactionRepsonse> getTransactionFormAccountId(String accountId);
}

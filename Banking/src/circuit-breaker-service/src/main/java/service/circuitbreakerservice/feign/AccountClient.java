package service.circuitbreakerservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import service.circuitbreakerservice.DTo.AccountDto;


import java.util.UUID;

@FeignClient(name = "api-gateway", url = "http://localhost:8080/accounts/**")

public interface AccountClient {
    @GetMapping("/account/{accountId}")
    AccountDto getAccount(@PathVariable("accountId") UUID acId);
}

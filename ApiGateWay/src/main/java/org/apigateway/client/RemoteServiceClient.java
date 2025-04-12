package org.apigateway.client;

import org.apigateway.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;

//@FeignClient(name = "remote-service", configuration = FeignClientConfig.class)
//sample working feign client
//@FeignClient(name = "account-service", url = "http://localhost:8081", configuration = FeignClientConfig.class)
public class RemoteServiceClient {
    // Get account details by ID
//    @GetMapping("/account/{id}")
//    Account getAccountById(@PathVariable("id") Long accountId);
//
//    // Create a new account
//    @PostMapping("/account/create")
//    Account createAccount(@RequestBody Account account);
}

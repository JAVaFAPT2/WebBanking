package service.circuitbreakerservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import service.circuitbreakerservice.DTo.UserDto;


import java.util.UUID;

@FeignClient(name = "api-gateway", url = "http://localhost:8080/users/")
public interface UserClient {
    @GetMapping("/users/{userId}")
    UserDto getUser(@PathVariable("userId") UUID userId);
}
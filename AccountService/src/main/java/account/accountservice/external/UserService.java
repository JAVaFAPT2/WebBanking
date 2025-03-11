package account.accountservice.external;

import account.accountservice.model.dto.external.UserDTO;
import org.springframework.cloud.netflix.feign.FeignAutoConfiguration;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@Service
@FeignClient(name = "user-service", configuration = FeignAutoConfiguration.class)
public interface UserService {


    @GetMapping("/user/{userId}")
    ResponseEntity<UserDTO> readUserById (@PathVariable Long userId);
 }

package account.accountservice.external;

import account.accountservice.model.dto.external.SeqenceDTO;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
@Service
@FeignClient(name = "sequence-generator")
public interface SequenceService {

    @PostMapping("/sequence")
    SeqenceDTO generateAccNumber();
}

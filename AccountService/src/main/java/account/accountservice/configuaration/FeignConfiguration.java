package account.accountservice.configuaration;

import feign.codec.ErrorDecoder;
import org.springframework.cloud.netflix.feign.FeignClientProperties;
import org.springframework.context.annotation.Bean;

public class FeignConfiguration extends FeignClientProperties.FeignClientConfiguration {
   @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignClientErrorDecoder();
    }
}

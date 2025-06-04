package org.apigateway.config;

import feign.Feign;
import feign.Logger;
import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class FeignClientConfig {
    @Bean
    public Request.Options feignRequestOptions() {
        return new Request.Options(
                5000, TimeUnit.MILLISECONDS,
                5000, TimeUnit.MILLISECONDS,
                true
        );
    }
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}

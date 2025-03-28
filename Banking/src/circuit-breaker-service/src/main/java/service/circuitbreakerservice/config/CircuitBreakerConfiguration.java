package service.circuitbreakerservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;

import java.time.Duration;

@Configuration
public class CircuitBreakerConfiguration {
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.ofDefaults();
    }

    @Bean
    public CircuitBreaker userCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("userService");
    }

    @Bean
    public CircuitBreaker accountCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("accountService");
    }
    @Bean
    public CircuitBreakerConfig defaultCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(5))
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .permittedNumberOfCallsInHalfOpenState(5)
                .build();
    }
}

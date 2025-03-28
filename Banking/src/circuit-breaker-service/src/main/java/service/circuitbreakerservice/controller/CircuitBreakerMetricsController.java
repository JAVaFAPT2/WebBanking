package service.circuitbreakerservice.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker.Metrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/circuit-breaker/metrics")
public class CircuitBreakerMetricsController {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    public CircuitBreakerMetricsController(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @GetMapping
    public Map<String, Object> getCircuitBreakerMetrics() {
        Map<String, Object> response = new HashMap<>();

        circuitBreakerRegistry.getAllCircuitBreakers().forEach(circuitBreaker -> {
            Map<String, Object> metricsMap = new HashMap<>();

            CircuitBreaker.State state = circuitBreaker.getState();
            Metrics metrics = circuitBreaker.getMetrics();

            metricsMap.put("state", state.toString());
            metricsMap.put("failureRate", metrics.getFailureRate());
            metricsMap.put("slowCallRate", metrics.getSlowCallRate());
            metricsMap.put("bufferedCalls", metrics.getNumberOfBufferedCalls());
            metricsMap.put("failedCalls", metrics.getNumberOfFailedCalls());
            metricsMap.put("successfulCalls", metrics.getNumberOfSuccessfulCalls());

            // Using the circuit breaker name
            response.put(circuitBreaker.getName(), metricsMap);
        });

        return response;
    }
}

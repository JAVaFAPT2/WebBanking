package org.apigateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;

import org.apigateway.filter.RBACFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Configuration
public class GatewayConfig {


    @Autowired
    private RBACFilter rbacFilter;
    // Resilience4j RateLimiter configuration
    @Bean
    public RateLimiter rateLimiter(RateLimiterRegistry rateLimiterRegistry) {
        return rateLimiterRegistry.rateLimiter("rateLimiter", RateLimiterConfig.custom()
                .limitForPeriod(10)  // 10 requests per second
                .limitRefreshPeriod(Duration.ofSeconds(1))  // Refresh every second
                .timeoutDuration(Duration.ofMillis(500))  // Timeout duration
                .build());
    }
    // Resilience4j CircuitBreaker configuration
    @Bean
    public CircuitBreaker circuitBreaker(CircuitBreakerRegistry circuitBreakerRegistry) {
        return circuitBreakerRegistry.circuitBreaker("circuitBreaker", CircuitBreakerConfig.custom()
                .slidingWindowSize(100)
                .failureRateThreshold(50)  // Fail if 50% of requests fail
                .waitDurationInOpenState(Duration.ofMillis(10000))  // 10 seconds in open state
                .permittedNumberOfCallsInHalfOpenState(10)  // Half-open state allows 10 calls
                .build());
    }

    // Create WebClient for making API calls with resilience features
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
    // Custom route configuration
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Public routes - no RBAC required
                .route("auth-service", r -> r.path("/api/auth/**")
                        .uri("lb://auth-service"))

                // User service routes - require USER role
                .route("user-service", r -> r.path("/api/users/**")
                        .filters(f -> f.filter(rbacFilter.apply(config ->
                                config.setRequiredRoles(Arrays.asList("USER", "ADMIN")))))
                        .uri("lb://user-service"))

                // Account service routes - require USER role
                .route("account-service", r -> r.path("/api/accounts/**")
                        .filters(f -> f.filter(rbacFilter.apply(config ->
                                config.setRequiredRoles(Arrays.asList("USER", "ADMIN")))))
                        .uri("lb://account-service"))

                // Fund transfer routes - require USER role
                .route("fund-transfer-service", r -> r.path("/api/transfers/**")
                        .filters(f -> f.filter(rbacFilter.apply(config ->
                                config.setRequiredRoles(Arrays.asList("USER", "ADMIN")))))
                        .uri("lb://fund-transfer-service"))

                // Transaction history routes - require USER role
                .route("transaction-service", r -> r.path("/api/transactions/**")
                        .filters(f -> f.filter(rbacFilter.apply(config ->
                                config.setRequiredRoles(Arrays.asList("USER", "ADMIN")))))
                        .uri("lb://transaction-service"))

                // Admin routes - require ADMIN role
                .route("admin-service", r -> r.path("/api/admin/**")
                        .filters(f -> f.filter(rbacFilter.apply(config ->
                                config.setRequiredRoles(List.of("ADMIN")))))
                        .uri("lb://admin-service"))

                // Bank manager routes - require MANAGER role
                .route("manager-service", r -> r.path("/api/management/**")
                        .filters(f -> f.filter(rbacFilter.apply(config ->
                                config.setRequiredRoles(Arrays.asList("MANAGER", "ADMIN")))))
                        .uri("lb://manager-service"))

                .build();
    }
}

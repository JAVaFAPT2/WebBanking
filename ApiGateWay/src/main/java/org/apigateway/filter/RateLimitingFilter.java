package org.apigateway.filter;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Component
public class RateLimitingFilter extends AbstractGatewayFilterFactory<RateLimitingFilter.Config> {
    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);
    private final Map<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();
    private RateLimiterRegistry rateLimiterRegistry;

    @Value("${rate-limit.default-limit}")
    private int defaultLimit;

    @Value("${rate-limit.default-refresh-period}")
    private int defaultRefreshPeriod;

    @Value("${rate-limit.default-timeout-duration}")
    private int defaultTimeoutDuration;

    public RateLimitingFilter() {
        super(Config.class);
    }

    @PostConstruct
    public void init() {
        // Ensure values are valid
        int limit = Math.max(defaultLimit, 1);  // At least 1
        int refreshPeriod = Math.max(defaultRefreshPeriod, 100);  // At least 100ms
        int timeoutDuration = Math.max(defaultTimeoutDuration, 0);  // At least 0

        logger.info("Initializing RateLimitingFilter with limit={}, refreshPeriod={}ms, timeoutDuration={}ms",
                limit, refreshPeriod, timeoutDuration);

        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMillis(refreshPeriod))
                .limitForPeriod(limit)
                .timeoutDuration(Duration.ofMillis(timeoutDuration))
                .build();

        this.rateLimiterRegistry = RateLimiterRegistry.of(config);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String clientId = resolveClientId(request);

            // Create or get a rate limiter for this client ID
            RateLimiter rateLimiter = rateLimiters.computeIfAbsent(clientId, createRateLimiter(config));

            boolean allowed = rateLimiter.acquirePermission();
            if (allowed) {
                // Add rate limit headers
                ServerHttpResponse response = exchange.getResponse();
                response.getHeaders().add("X-RateLimit-Limit",
                        String.valueOf(rateLimiter.getRateLimiterConfig().getLimitForPeriod()));

                return chain.filter(exchange);
            } else {
                logger.warn("Rate limit exceeded for client: {}", clientId);
                return onRateLimitExceeded(exchange.getResponse());
            }
        };
    }

    private String resolveClientId(ServerHttpRequest request) {
        // First try to get from auth header X-Auth-User-ID
        String userId = request.getHeaders().getFirst("X-Auth-User-ID");
        if (userId != null && !userId.isEmpty()) {
            return "user-" + userId;
        }

        // Fallback to IP address for unauthenticated requests
        String clientIp = request.getRemoteAddress() != null ?
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
        return "ip-" + clientIp;
    }

    private Function<String, RateLimiter> createRateLimiter(Config config) {
        return clientId -> {
            // Use config values but ensure they're valid
            int limit = Math.max(config.getLimitForPeriod(), 1);  // At least 1
            int refreshPeriod = Math.max(config.getRefreshPeriod(), 100);  // At least 100ms

            RateLimiterConfig clientConfig = RateLimiterConfig.custom()
                    .limitRefreshPeriod(Duration.ofMillis(refreshPeriod))
                    .limitForPeriod(limit)
                    .timeoutDuration(Duration.ofMillis(defaultTimeoutDuration))
                    .build();

            logger.debug("Created rate limiter for client: {} with limit: {}, refreshPeriod: {}ms",
                    clientId, limit, refreshPeriod);

            return rateLimiterRegistry.rateLimiter(clientId, clientConfig);
        };
    }

    private Mono<Void> onRateLimitExceeded(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        errorResponse.put("error", "Too Many Requests");
        errorResponse.put("message", "You have exceeded the API call rate limit");

        // Retry-After header indicates when to try again
        response.getHeaders().add("Retry-After", "1");

        byte[] bytes = errorResponseToJson(errorResponse).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    private String errorResponseToJson(Map<String, Object> errorResponse) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : errorResponse.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append("\"").append(entry.getKey()).append("\":");
            if (entry.getValue() instanceof String) {
                sb.append("\"").append(entry.getValue()).append("\"");
            } else {
                sb.append(entry.getValue());
            }
        }
        sb.append("}");
        return sb.toString();
    }

    @Setter
    @Getter
    public static class Config {
        private int limitForPeriod = 10;
        private int refreshPeriod = 1000;
    }
}

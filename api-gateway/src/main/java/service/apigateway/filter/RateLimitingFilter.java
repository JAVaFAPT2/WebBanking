package service.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import service.apigateway.config.GatewayProperties;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * Gateway filter for limiting the number of HTTP calls per client.
 */
@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private static final String TIME_PERIOD = "hour";

    private final RateLimitingRepository rateLimitingRepository;
    private final long rateLimit;

    @Autowired
    public RateLimitingFilter(RateLimitingRepository rateLimitingRepository,
                              GatewayProperties gatewayProperties) {
        this.rateLimitingRepository = rateLimitingRepository;
        this.rateLimit = gatewayProperties.getRateLimiting().getLimit();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Get client identifier
        String id = getId(exchange);
        Date date = getPeriod();

        // Check current rate limit
        return rateLimitingRepository.getCounter(id, TIME_PERIOD, date)
                .flatMap(count -> {
                    log.debug("Rate limiting for user {} at {} - {}", id, date, count);

                    if (count > rateLimit) {
                        return apiLimitExceeded(exchange);
                    } else {
                        // Increment counter and continue
                        return rateLimitingRepository.incrementCounter(id, TIME_PERIOD, date)
                                .then(chain.filter(exchange));
                    }
                });
    }



    private Mono<Void> apiLimitExceeded(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap("API rate limit exceeded".getBytes()))
        );
    }

    /**
     * The ID that will identify the limit: the user login or the user IP address.
     */
    private String getId(ServerWebExchange exchange) {
        // Try to get user from security context
        return Mono.deferContextual(ctx ->
                        Mono.justOrEmpty(ctx.getOrEmpty("user"))
                                .cast(String.class)
                )
                .defaultIfEmpty(getIpAddress(exchange))
                .block(); // Note: blocking call in reactive context - consider refactoring
    }

    /**
     * Get client IP address from request
     */
    private String getIpAddress(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        return Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress();
    }

    /**
     * The period for which the rate is calculated.
     */
    private Date getPeriod() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear(Calendar.MILLISECOND);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MINUTE);
        return calendar.getTime();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}

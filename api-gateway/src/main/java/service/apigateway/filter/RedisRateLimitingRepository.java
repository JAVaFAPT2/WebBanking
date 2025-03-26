package service.apigateway.filter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

/**
 * Redis implementation of the rate limiting repository
 */
@Component
public class RedisRateLimitingRepository implements RateLimitingRepository {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH");

    public RedisRateLimitingRepository(@Qualifier("reactiveStringRedisTemplate") ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Long> getCounter(String id, String period, Date date) {
        String key = generateKey(id, period, date);
        return redisTemplate.opsForValue().get(key)
                .map(Long::parseLong)
                .defaultIfEmpty(0L);
    }

    @Override
    public Mono<Long> incrementCounter(String id, String period, Date date) {
        String key = generateKey(id, period, date);
        return redisTemplate.opsForValue().increment(key)
                .flatMap(count -> {
                    // Set expiry for the key if it's new
                    if (count == 1) {
                        return redisTemplate.expire(key, getExpiryForPeriod(period))
                                .thenReturn(count);
                    }
                    return Mono.just(count);
                });
    }

    /**
     * Generate Redis key for rate limiting
     */
    private String generateKey(String id, String period, Date date) {
        return "rate-limit:" + id + ":" + period + ":" + formatter.format(date);
    }

    /**
     * Get expiry duration based on period
     */
    private Duration getExpiryForPeriod(String period) {
        switch (period.toLowerCase()) {
            case "second":
                return Duration.ofSeconds(1);
            case "minute":
                return Duration.ofMinutes(1);
            case "day":
                return Duration.ofDays(1);
            default:
                return Duration.ofHours(1);
        }
    }
}

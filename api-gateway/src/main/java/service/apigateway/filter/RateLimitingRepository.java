package service.apigateway.filter;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Date;

/**
 * Repository interface for rate limiting counters
 */

@Repository
public interface RateLimitingRepository {

    /**
     * Get the counter for a specific ID and period
     */
    Mono<Long> getCounter(String id, String period, Date date);

    /**
     * Increment the counter for a specific ID and period
     */
    Mono<Long> incrementCounter(String id, String period, Date date);
}

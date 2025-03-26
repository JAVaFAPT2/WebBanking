package service.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "gateway", ignoreUnknownFields = false)
public class GatewayProperties {

    private final RateLimiting rateLimiting = new RateLimiting();

    public RateLimiting getRateLimiting() {
        return rateLimiting;
    }

    public static class RateLimiting {
        private long limit = 100000;

        public long getLimit() {
            return limit;
        }

        public void setLimit(long limit) {
            this.limit = limit;
        }
    }
}
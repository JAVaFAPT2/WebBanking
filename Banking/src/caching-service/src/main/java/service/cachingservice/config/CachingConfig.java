package service.cachingservice.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Caching configuration class.
 * It defines the cache manager bean to be used across the application.
 */
@Configuration
public class CachingConfig {

    @Bean
    public CacheManager cacheManager() {
        // Define a cache manager with a cache named "dataCache"
        return new ConcurrentMapCacheManager("dataCache");
    }
}

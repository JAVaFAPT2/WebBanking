package org.cachingservice.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.*;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CachingConfig implements CachingConfigurer {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // Default cache configuration
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues()
                .prefixCacheNameWith("banking-cache:"); // Add a prefix to all cache keys

        // Configure cache TTLs for specific caches
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // User-related caches (short-lived)
        cacheConfigurations.put("userProfile",
                defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("userPreferences",
                defaultCacheConfig.entryTtl(Duration.ofMinutes(15)));

        // Account-related caches (medium-lived)
        cacheConfigurations.put("accountSummary",
                defaultCacheConfig.entryTtl(Duration.ofMinutes(3)));
        cacheConfigurations.put("accountDetails",
                defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("accountTransactions",
                defaultCacheConfig.entryTtl(Duration.ofMinutes(2)));

        // Reference data caches (long-lived)
        cacheConfigurations.put("currencies",
                defaultCacheConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigurations.put("countries",
                defaultCacheConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigurations.put("bankBranches",
                defaultCacheConfig.entryTtl(Duration.ofHours(12)));

        // System configuration caches
        cacheConfigurations.put("systemConfig",
                defaultCacheConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("featureFlags",
                defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));

        // Create the cache manager
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }

    @Override
    public CacheResolver cacheResolver() {
        return null; // Use the default
    }

    @Override
    public KeyGenerator keyGenerator() {
        return new SimpleKeyGenerator();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        // Custom error handler to gracefully handle Redis connection issues
        return new SimpleCacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                // Log the error but don't rethrow - allows the application to function when Redis is down
                // In a production environment, you might want to add monitoring/alerting here
                System.err.println("Error getting from cache: " + cache.getName() + " with key: " + key);
                System.err.println("Error: " + exception.getMessage());
            }
        };
    }

    // Custom key generator for more complex scenarios
    @Bean(name = "bankingKeyGenerator")
    public KeyGenerator customKeyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getSimpleName());
            sb.append(":");
            sb.append(method.getName());
            sb.append(":");
            for (Object param : params) {
                sb.append(param != null ? param.toString() : "null");
                sb.append(":");
            }
            return sb.toString();
        };
    }

    // Other methods for configuring cache behavior
    // Example of using the cache manager in a service
    

//    // Using default cache settings
//    @Cacheable(value = "accountSummary", key = "#accountId")
//    public AccountSummary getAccountSummary(String accountId) {
//        // Implementation
//        return null;
//    }
//
//    // Using custom key generator
//    @Cacheable(value = "userProfile", keyGenerator = "bankingKeyGenerator")
//    public UserProfile getUserProfile(String userId, boolean includePreferences) {
//        // Implementation
//        return null;
//    }
//
//    // Cache update
//    @CachePut(value = "accountDetails", key = "#account.id")
//    public AccountDetails updateAccountDetails(Account account) {
//        // Implementation
//        return null;
//    }
//
//    // Cache eviction
//    @CacheEvict(value = "accountTransactions", key = "#accountId")
//    public void refreshTransactions(String accountId) {
//        // Implementation
//    }
}

package org.cachingservice.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.ReadMode;
import org.redisson.config.SubscriptionMode;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.database:0}")
    private int redisDatabase;

    @Value("${spring.data.redis.timeout:3000ms}")
    private String redisTimeout;

    @Value("${spring.data.redis.cluster.nodes:}")
    private String clusterNodes;

    @Value("${spring.data.redis.sentinel.master:}")
    private String sentinelMaster;

    @Value("${spring.data.redis.sentinel.nodes:}")
    private String sentinelNodes;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        int timeout = Integer.parseInt(redisTimeout.replace("ms", ""));

        if (clusterNodes != null && !clusterNodes.isEmpty()) {
            // Redis Cluster configuration
            String[] nodeAddresses = clusterNodes.split(",");
            for (int i = 0; i < nodeAddresses.length; i++) {
                nodeAddresses[i] = "redis://" + nodeAddresses[i];
            }

            config.useClusterServers()
                    .addNodeAddress(nodeAddresses)
                    .setPassword(redisPassword.isEmpty() ? null : redisPassword)
                    .setReadMode(ReadMode.MASTER_SLAVE)
                    .setSubscriptionMode(SubscriptionMode.MASTER)
                    .setTimeout(timeout);
        } else if (sentinelMaster != null && !sentinelMaster.isEmpty() && sentinelNodes != null && !sentinelNodes.isEmpty()) {
            // Redis Sentinel configuration
            String[] sentinelAddresses = sentinelNodes.split(",");
            for (int i = 0; i < sentinelAddresses.length; i++) {
                sentinelAddresses[i] = "redis://" + sentinelAddresses[i];
            }

            config.useSentinelServers()
                    .setMasterName(sentinelMaster)
                    .addSentinelAddress(sentinelAddresses)
                    .setPassword(redisPassword.isEmpty() ? null : redisPassword)
                    .setDatabase(redisDatabase)
                    .setReadMode(ReadMode.MASTER_SLAVE)
                    .setTimeout(timeout);
        } else {
            // Single Redis server configuration
            config.useSingleServer()
                    .setAddress("redis://" + redisHost + ":" + redisPort)
                    .setPassword(redisPassword.isEmpty() ? null : redisPassword)
                    .setDatabase(redisDatabase)
                    .setTimeout(timeout);
        }

        return Redisson.create(config);
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(RedissonClient redissonClient) {
        return new RedissonConnectionFactory(redissonClient);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use StringRedisSerializer for keys
        template.setKeySerializer(new StringRedisSerializer());

        // Use GenericJackson2JsonRedisSerializer for values
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        // Also set serializers for hash keys and values
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}

package org.cachingservice.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Repository
public class CacheRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public CacheRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // String operations
    public void setString(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void setStringWithExpiry(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public String getString(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value.toString();
    }

    // List operations
    public void rightPushToList(String key, Object value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    public void leftPushToList(String key, Object value) {
        redisTemplate.opsForList().leftPush(key, value);
    }

    public List<Object> getListRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    public Object leftPopFromList(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    public Object rightPopFromList(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    // Hash operations
    public void putAllToHash(String key, Map<Object, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    public void putToHash(String key, Object hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    public Object getFromHash(String key, Object hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    public Map<Object, Object> getEntireHash(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public boolean deleteFromHash(String key, Object... hashKeys) {
        return redisTemplate.opsForHash().delete(key, hashKeys) > 0;
    }

    // Set operations
    public void addToSet(String key, Object... values) {
        redisTemplate.opsForSet().add(key, values);
    }

    public Set<Object> getSetMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    public boolean isMemberOfSet(String key, Object value) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
    }

    public boolean removeFromSet(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values) > 0;
    }

    // ZSet (Sorted Set) operations
    public boolean addToZSet(String key, Object value, double score) {
        return Boolean.TRUE.equals(redisTemplate.opsForZSet().add(key, value, score));
    }

    public Set<Object> getZSetRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    public Set<Object> getZSetRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    public boolean removeFromZSet(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(key, values) > 0;
    }

    // Key operations
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public boolean expire(String key, long timeout, TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
    }

    public Set<String> getKeys(String pattern) {
        return redisTemplate.keys(pattern);
    }
}

package org.cachingservice.service;

import org.cachingservice.repository.CacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
/**
 * Service layer for caching operations.
 * This service provides business logic on top of the repository layer,
 * handling type conversions, validation, and higher-level operations.
 */
@Service
public class CachingService {

    private final CacheRepository cacheRepository;

    @Autowired
    public CachingService(CacheRepository cacheRepository) {
        this.cacheRepository = cacheRepository;
    }

    // General operations
    public void put(String key, Object value, long ttl, TimeUnit timeUnit) {
        if (value instanceof String) {
            cacheRepository.setStringWithExpiry(key, (String) value, ttl, timeUnit);
        } else {
            // For non-string values, serialize as JSON and store as string
            // This is a simplified approach - in a real app, you might want to use different data structures
            // based on the value type or have more sophisticated serialization
            cacheRepository.setStringWithExpiry(key, value.toString(), ttl, timeUnit);
        }
    }

    public void put(String key, Object value) {
        if (value instanceof String) {
            cacheRepository.setString(key, (String) value);
        } else {
            cacheRepository.setString(key, value.toString());
        }
    }

    public Object get(String key) {
        if (!cacheRepository.hasKey(key)) {
            return null;
        }
        return cacheRepository.getString(key);
    }

    public boolean hasKey(String key) {
        return cacheRepository.hasKey(key);
    }

    public void delete(String key) {
        cacheRepository.delete(key);
    }

    public boolean expire(String key, long timeout, TimeUnit unit) {
        return cacheRepository.expire(key, timeout, unit);
    }

    public Set<String> getKeys(String pattern) {
        return cacheRepository.getKeys(pattern);
    }

    // List operations
    public void addToList(String key, Object value, boolean addToRight) {
        if (addToRight) {
            cacheRepository.rightPushToList(key, value);
        } else {
            cacheRepository.leftPushToList(key, value);
        }
    }

    public List<Object> getList(String key, long start, long end) {
        return cacheRepository.getListRange(key, start, end);
    }

    public Object popFromList(String key, boolean popFromRight) {
        if (popFromRight) {
            return cacheRepository.rightPopFromList(key);
        } else {
            return cacheRepository.leftPopFromList(key);
        }
    }

    // Hash operations
    public void putHash(String key, Object hashKey, Object value) {
        cacheRepository.putToHash(key, hashKey, value);
    }

    public void putAllHash(String key, Map<Object, Object> map) {
        cacheRepository.putAllToHash(key, map);
    }

    public Object getHash(String key, Object hashKey) {
        return cacheRepository.getFromHash(key, hashKey);
    }

    public Map<Object, Object> getAllHash(String key) {
        return cacheRepository.getEntireHash(key);
    }

    public boolean deleteHash(String key, Object... hashKeys) {
        return cacheRepository.deleteFromHash(key, hashKeys);
    }

    // Set operations
    public void addToSet(String key, Object... values) {
        cacheRepository.addToSet(key, values);
    }

    public Set<Object> getSetMembers(String key) {
        return cacheRepository.getSetMembers(key);
    }

    public boolean isInSet(String key, Object value) {
        return cacheRepository.isMemberOfSet(key, value);
    }

    public boolean removeFromSet(String key, Object... values) {
        return cacheRepository.removeFromSet(key, values);
    }

    // Sorted Set operations
    public boolean addToSortedSet(String key, Object value, double score) {
        return cacheRepository.addToZSet(key, value, score);
    }

    public Set<Object> getSortedSetRange(String key, long start, long end) {
        return cacheRepository.getZSetRange(key, start, end);
    }

    public Set<Object> getSortedSetByScore(String key, double min, double max) {
        return cacheRepository.getZSetRangeByScore(key, min, max);
    }

    public boolean removeFromSortedSet(String key, Object... values) {
        return cacheRepository.removeFromZSet(key, values);
    }

    // Counter operations
    public Long increment(String key, long delta) {
        // First check if the key exists, if not initialize it
        if (!cacheRepository.hasKey(key)) {
            cacheRepository.setString(key, "0");
        }

        // Use Redis string increment operation
        // This is implemented by converting the string to a number
        String value = cacheRepository.getString(key);
        long currentValue;
        try {
            currentValue = Long.parseLong(value);
        } catch (NumberFormatException e) {
            // If the value is not a number, reset it to 0
            currentValue = 0;
            cacheRepository.setString(key, "0");
        }

        long newValue = currentValue + delta;
        cacheRepository.setString(key, String.valueOf(newValue));
        return newValue;
    }

    // Cache statistics and management
    public long getSize() {
        return cacheRepository.getKeys("*").size();
    }

    public void flushAll() {
        Set<String> allKeys = cacheRepository.getKeys("*");
        for (String key : allKeys) {
            cacheRepository.delete(key);
        }
    }

    public void flushByPattern(String pattern) {
        Set<String> matchingKeys = cacheRepository.getKeys(pattern);
        for (String key : matchingKeys) {
            cacheRepository.delete(key);
        }
    }
}

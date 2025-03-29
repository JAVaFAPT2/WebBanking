package service.cachingservice.repository;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple repository that uses a ConcurrentHashMap to mimic a cache store.
 */
@Repository
public class CacheRepository {

    private final Map<String, String> cacheStore = new ConcurrentHashMap<>();

    /**
     * Retrieves an item from the cache.
     *
     * @param key The key of the cache entry.
     * @return The cached value, or null if not present.
     */
    public String get(String key) {
        return cacheStore.get(key);
    }

    /**
     * Puts an item into the cache.
     *
     * @param key   The key of the cache entry.
     * @param value The value to be cached.
     */
    public void put(String key, String value) {
        cacheStore.put(key, value);
    }

    /**
     * Removes an item from the cache.
     *
     * @param key The key of the cache entry to remove.
     */
    public void evict(String key) {
        cacheStore.remove(key);
    }

    /**
     * Clears all items from the cache.
     */
    public void clear() {
        cacheStore.clear();
    }
}

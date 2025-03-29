package service.cachingservice.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * A simple service that caches the result of a time-consuming method.
 */
@Service
public class CachingService {

    /**
     * Retrieves data for a given id. This method is cached so that if it is called again with the same id,
     * the cached result is returned immediately.
     *
     * @param id The identifier used as the cache key.
     * @return The data corresponding to the id.
     */
    @Cacheable(value = "dataCache", key = "#id")
    public String getData(String id) {
        // Simulate a time-consuming operation (e.g., a remote API call or complex computation)
        try {
            Thread.sleep(2000); // 2-second delay to simulate processing time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Data for id: " + id;
    }
}
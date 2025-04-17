package org.cachingservice.controller;

import org.cachingservice.service.CachingService;
import org.cachingservice.service.DistributedLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/cache")
public class CacheController {

    private final CachingService cachingService;
    private final DistributedLockService lockService;

    @Autowired
    public CacheController(CachingService cachingService, DistributedLockService lockService) {
        this.cachingService = cachingService;
        this.lockService = lockService;
    }

    // String operations
    @PostMapping("/string/{key}")
    public ResponseEntity<String> setString(@PathVariable String key, @RequestBody String value) {
        cachingService.put(key, value);
        return new ResponseEntity<>("String value stored successfully", HttpStatus.CREATED);
    }

    @PostMapping("/string/{key}/ttl")
    public ResponseEntity<String> setStringWithExpiry(
            @PathVariable String key,
            @RequestBody String value,
            @RequestParam long timeout,
            @RequestParam(defaultValue = "SECONDS") TimeUnit unit) {
        cachingService.put(key, value, timeout, unit);
        return new ResponseEntity<>("String value stored with TTL successfully", HttpStatus.CREATED);
    }

    @GetMapping("/string/{key}")
    public ResponseEntity<String> getString(@PathVariable String key) {
        if (!cachingService.hasKey(key)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Object value = cachingService.get(key);
        return new ResponseEntity<>(value.toString(), HttpStatus.OK);
    }

    // List operations
    @PostMapping("/list/{key}/right")
    public ResponseEntity<String> rightPushToList(@PathVariable String key, @RequestBody Object value) {
        cachingService.addToList(key, value, true);
        return new ResponseEntity<>("Value pushed to right of list", HttpStatus.CREATED);
    }

    @PostMapping("/list/{key}/left")
    public ResponseEntity<String> leftPushToList(@PathVariable String key, @RequestBody Object value) {
        cachingService.addToList(key, value, false);
        return new ResponseEntity<>("Value pushed to left of list", HttpStatus.CREATED);
    }

    @GetMapping("/list/{key}")
    public ResponseEntity<List<Object>> getListRange(
            @PathVariable String key,
            @RequestParam(defaultValue = "0") long start,
            @RequestParam(defaultValue = "-1") long end) {
        List<Object> values = cachingService.getList(key, start, end);
        return new ResponseEntity<>(values, HttpStatus.OK);
    }

    @DeleteMapping("/list/{key}/left")
    public ResponseEntity<Object> leftPopFromList(@PathVariable String key) {
        Object value = cachingService.popFromList(key, false);
        if (value == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(value, HttpStatus.OK);
    }

    @DeleteMapping("/list/{key}/right")
    public ResponseEntity<Object> rightPopFromList(@PathVariable String key) {
        Object value = cachingService.popFromList(key, true);
        if (value == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(value, HttpStatus.OK);
    }

    // Hash operations
    @PostMapping("/hash/{key}")
    public ResponseEntity<String> putAllToHash(@PathVariable String key, @RequestBody Map<Object, Object> map) {
        cachingService.putAllHash(key, map);
        return new ResponseEntity<>("Hash values stored successfully", HttpStatus.CREATED);
    }

    @PostMapping("/hash/{key}/{hashKey}")
    public ResponseEntity<String> putToHash(
            @PathVariable String key,
            @PathVariable String hashKey,
            @RequestBody Object value) {
        cachingService.putHash(key, hashKey, value);
        return new ResponseEntity<>("Hash value stored successfully", HttpStatus.CREATED);
    }

    @GetMapping("/hash/{key}/{hashKey}")
    public ResponseEntity<Object> getFromHash(@PathVariable String key, @PathVariable String hashKey) {
        Object value = cachingService.getHash(key, hashKey);
        if (value == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(value, HttpStatus.OK);
    }

    @GetMapping("/hash/{key}")
    public ResponseEntity<Map<Object, Object>> getEntireHash(@PathVariable String key) {
        Map<Object, Object> map = cachingService.getAllHash(key);
        if (map.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @DeleteMapping("/hash/{key}/{hashKey}")
    public ResponseEntity<String> deleteFromHash(@PathVariable String key, @PathVariable String hashKey) {
        boolean deleted = cachingService.deleteHash(key, hashKey);
        if (!deleted) {
            return new ResponseEntity<>("Hash key not found", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>("Hash key deleted successfully", HttpStatus.OK);
    }

    // Set operations
    @PostMapping("/set/{key}")
    public ResponseEntity<String> addToSet(@PathVariable String key, @RequestBody Object[] values) {
        cachingService.addToSet(key, values);
        return new ResponseEntity<>("Values added to set successfully", HttpStatus.CREATED);
    }

    @GetMapping("/set/{key}")
    public ResponseEntity<Set<Object>> getSetMembers(@PathVariable String key) {
        Set<Object> members = cachingService.getSetMembers(key);
        if (members.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(members, HttpStatus.OK);
    }

    @GetMapping("/set/{key}/contains")
    public ResponseEntity<Map<String, Boolean>> isMemberOfSet(
            @PathVariable String key,
            @RequestParam Object value) {
        boolean isMember = cachingService.isInSet(key, value);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isMember", isMember);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/set/{key}")
    public ResponseEntity<String> removeFromSet(@PathVariable String key, @RequestBody Object[] values) {
        boolean removed = cachingService.removeFromSet(key, values);
        if (!removed) {
            return new ResponseEntity<>("Values not found in set", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>("Values removed from set successfully", HttpStatus.OK);
    }

    // ZSet (Sorted Set) operations
    @PostMapping("/zset/{key}")
    public ResponseEntity<String> addToZSet(
            @PathVariable String key,
            @RequestParam Object value,
            @RequestParam double score) {
        boolean added = cachingService.addToSortedSet(key, value, score);
        if (!added) {
            return new ResponseEntity<>("Failed to add to sorted set", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Value added to sorted set successfully", HttpStatus.CREATED);
    }

    @GetMapping("/zset/{key}/range")
    public ResponseEntity<Set<Object>> getZSetRange(
            @PathVariable String key,
            @RequestParam(defaultValue = "0") long start,
            @RequestParam(defaultValue = "-1") long end) {
        Set<Object> values = cachingService.getSortedSetRange(key, start, end);
        if (values.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(values, HttpStatus.OK);
    }

    @GetMapping("/zset/{key}/score-range")
    public ResponseEntity<Set<Object>> getZSetRangeByScore(
            @PathVariable String key,
            @RequestParam double min,
            @RequestParam double max) {
        Set<Object> values = cachingService.getSortedSetByScore(key, min, max);
        if (values.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(values, HttpStatus.OK);
    }

    @DeleteMapping("/zset/{key}")
    public ResponseEntity<String> removeFromZSet(@PathVariable String key, @RequestBody Object[] values) {
        boolean removed = cachingService.removeFromSortedSet(key, values);
        if (!removed) {
            return new ResponseEntity<>("Values not found in sorted set", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>("Values removed from sorted set successfully", HttpStatus.OK);
    }

    // Distributed lock operations
    @PostMapping("/lock/{lockKey}")
    public ResponseEntity<Map<String, Object>> executeWithLock(
            @PathVariable String lockKey,
            @RequestParam(defaultValue = "10") long waitTime,
            @RequestParam(defaultValue = "30") long leaseTime) {

        try {
            Map<String, Object> result = lockService.executeWithLock(
                    lockKey, waitTime, leaseTime, TimeUnit.SECONDS,
                    () -> {
                        // Simulate some work
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }

                        Map<String, Object> response = new HashMap<>();
                        response.put("status", "success");
                        response.put("message", "Operation executed with lock: " + lockKey);
                        response.put("timestamp", System.currentTimeMillis());
                        return response;
                    });

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/lock/{lockKey}/status")
    public ResponseEntity<Map<String, Boolean>> checkLockStatus(@PathVariable String lockKey) {
        boolean locked = lockService.isLocked(lockKey);
        Map<String, Boolean> response = new HashMap<>();
        response.put("locked", locked);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/lock/{lockKey}")
    public ResponseEntity<String> forcefullyUnlock(@PathVariable String lockKey) {
        lockService.forceUnlock(lockKey);
        return new ResponseEntity<>("Lock forcefully released", HttpStatus.OK);
    }

    @PostMapping("/lock/{lockKey}/simple")
    public ResponseEntity<Map<String, Object>> executeWithSimpleLock(@PathVariable String lockKey) {
        Map<String, Object> result = lockService.executeWithLock(
                lockKey,
                () -> {
                    // Simulate some work
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("message", "Operation executed with simple lock: " + lockKey);
                    response.put("timestamp", System.currentTimeMillis());
                    return response;
                });

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // Cache management endpoints
    @GetMapping("/stats/size")
    public ResponseEntity<Map<String, Long>> getCacheSize() {
        long size = cachingService.getSize();
        Map<String, Long> response = new HashMap<>();
        response.put("size", size);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/flush/all")
    public ResponseEntity<String> flushAllCache() {
        cachingService.flushAll();
        return new ResponseEntity<>("All cache entries flushed successfully", HttpStatus.OK);
    }

    @DeleteMapping("/flush")
    public ResponseEntity<String> flushByPattern(@RequestParam String pattern) {
        cachingService.flushByPattern(pattern);
        return new ResponseEntity<>("Cache entries matching pattern flushed successfully", HttpStatus.OK);
    }

    // Key operations
    @DeleteMapping("/{key}")
    public ResponseEntity<String> delete(@PathVariable String key) {
        cachingService.delete(key);
        return new ResponseEntity<>("Key deleted successfully", HttpStatus.OK);
    }

    @GetMapping("/{key}/exists")
    public ResponseEntity<Map<String, Boolean>> hasKey(@PathVariable String key) {
        boolean exists = cachingService.hasKey(key);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{key}/expire")
    public ResponseEntity<String> expire(
            @PathVariable String key,
            @RequestParam long timeout,
            @RequestParam(defaultValue = "SECONDS") TimeUnit unit) {
        boolean success = cachingService.expire(key, timeout, unit);
        if (!success) {
            return new ResponseEntity<>("Key not found", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>("Expiry set successfully", HttpStatus.OK);
    }

    @GetMapping("/keys")
    public ResponseEntity<Set<String>> getKeys(@RequestParam(defaultValue = "*") String pattern) {
        Set<String> keys = cachingService.getKeys(pattern);
        return new ResponseEntity<>(keys, HttpStatus.OK);
    }

    @PostMapping("/increment/{key}")
    public ResponseEntity<Map<String, Long>> incrementValue(
            @PathVariable String key,
            @RequestParam(defaultValue = "1") long delta) {
        Long newValue = cachingService.increment(key, delta);
        Map<String, Long> response = new HashMap<>();
        response.put("value", newValue);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

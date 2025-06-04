package org.cachingservice.service;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class DistributedLockService {

    private final RedissonClient redissonClient;

    @Autowired
    public DistributedLockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * Execute a task with a distributed lock
     *
     * @param lockKey the lock key
     * @param waitTime maximum time to wait for the lock
     * @param leaseTime lock lease time (auto-released after this time)
     * @param timeUnit time unit for waitTime and leaseTime
     * @param task the task to execute when the lock is acquired
     * @return the result of the task
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit, Supplier<T> task) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean locked = lock.tryLock(waitTime, leaseTime, timeUnit);
            if (!locked) {
                throw new RuntimeException("Failed to acquire distributed lock for key: " + lockKey);
            }
            return task.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while acquiring distributed lock", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * Execute a task with a distributed lock (void version)
     */
    public void executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit, Runnable task) {
        executeWithLock(lockKey, waitTime, leaseTime, timeUnit, () -> {
            task.run();
            return null;
        });
    }

    /**
     * Execute a task with a distributed lock using default timing parameters
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> task) {
        return executeWithLock(lockKey, 10, 30, TimeUnit.SECONDS, task);
    }

    /**
     * Execute a task with a distributed lock using default timing parameters (void version)
     */
    public void executeWithLock(String lockKey, Runnable task) {
        executeWithLock(lockKey, 10, 30, TimeUnit.SECONDS, task);
    }

    /**
     * Check if a lock exists
     */
    public boolean isLocked(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isLocked();
    }

    /**
     * Force unlock a lock (use with caution)
     */
    public void forceUnlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.forceUnlock();
    }
}

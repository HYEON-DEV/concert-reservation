package kr.hhplus.be.server.lock;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisDistributedLockManager {

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
        """
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                return redis.call('DEL', KEYS[1])
            end
            return 0
        """,
        Long.class
    );

    private final StringRedisTemplate redisTemplate;

    public <T> T executeWithLock(
        String key,
        Duration ttl,
        Duration waitTimeout,
        Duration retryInterval,
        Supplier<T> action
    ) {
        String token = tryAcquire(key, ttl, waitTimeout, retryInterval);
        try {
            return action.get();
        } finally {
            release(key, token);
        }
    }

    public void executeWithLock(
        String key,
        Duration ttl,
        Duration waitTimeout,
        Duration retryInterval,
        Runnable action
    ) {
        executeWithLock(key, ttl, waitTimeout, retryInterval, () -> {
            action.run();
            return null;
        });
    }

    private String tryAcquire(
        String key,
        Duration ttl,
        Duration waitTimeout,
        Duration retryInterval
    ) {
        String token = UUID.randomUUID().toString();
        long deadline = System.nanoTime() + waitTimeout.toNanos();

        while (System.nanoTime() < deadline) {
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, token, ttl);
            if (Boolean.TRUE.equals(acquired)) {
                return token;
            }
            sleep(retryInterval);
        }

        throw new DistributedLockAcquisitionException("failed to acquire lock: " + key);
    }

    private void release(String key, String token) {
        redisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(key), token);
    }

    private void sleep(Duration retryInterval) {
        try {
            Thread.sleep(retryInterval.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("lock retry interrupted", e);
        }
    }
}

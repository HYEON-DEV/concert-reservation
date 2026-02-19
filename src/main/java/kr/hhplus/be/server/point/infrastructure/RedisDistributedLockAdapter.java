package kr.hhplus.be.server.point.infrastructure;

import java.time.Duration;
import java.util.function.Supplier;
import kr.hhplus.be.server.lock.RedisDistributedLockManager;
import kr.hhplus.be.server.point.application.port.DistributedLockPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisDistributedLockAdapter implements DistributedLockPort {

    private final RedisDistributedLockManager lockManager;

    @Override
    public <T> T executeWithLock(
        String key,
        Duration ttl,
        Duration waitTimeout,
        Duration retryInterval,
        Supplier<T> action
    ) {
        return lockManager.executeWithLock(key, ttl, waitTimeout, retryInterval, action);
    }

    @Override
    public void executeWithLock(
        String key,
        Duration ttl,
        Duration waitTimeout,
        Duration retryInterval,
        Runnable action
    ) {
        lockManager.executeWithLock(key, ttl, waitTimeout, retryInterval, action);
    }
}

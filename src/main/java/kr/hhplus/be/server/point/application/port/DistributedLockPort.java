package kr.hhplus.be.server.point.application.port;

import java.time.Duration;
import java.util.function.Supplier;

public interface DistributedLockPort {

    <T> T executeWithLock(
        String key,
        Duration ttl,
        Duration waitTimeout,
        Duration retryInterval,
        Supplier<T> action
    );

    void executeWithLock(
        String key,
        Duration ttl,
        Duration waitTimeout,
        Duration retryInterval,
        Runnable action
    );
}

package kr.hhplus.be.server.point.application.usecase;

import java.time.Duration;
import kr.hhplus.be.server.point.application.PointService;
import kr.hhplus.be.server.point.application.port.DistributedLockPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointCommandInteractor implements ChargePointUseCase, UsePointUseCase {

    private final DistributedLockPort distributedLockPort;
    private final PointService pointService;

    @Value("${app.lock.user-point.ttl-ms:3000}")
    private long lockTtlMs;

    @Value("${app.lock.user-point.wait-ms:2000}")
    private long lockWaitMs;

    @Value("${app.lock.user-point.retry-ms:50}")
    private long lockRetryMs;

    @Override
    public long charge(String userId, long amount) {
        return distributedLockPort.executeWithLock(
            lockKey(userId),
            Duration.ofMillis(lockTtlMs),
            Duration.ofMillis(lockWaitMs),
            Duration.ofMillis(lockRetryMs),
            () -> pointService.charge(userId, amount)
        );
    }

    @Override
    public void use(String userId, long amount) {
        distributedLockPort.executeWithLock(
            lockKey(userId),
            Duration.ofMillis(lockTtlMs),
            Duration.ofMillis(lockWaitMs),
            Duration.ofMillis(lockRetryMs),
            () -> pointService.use(userId, amount)
        );
    }

    private String lockKey(String userId) {
        return "lock:user_point:" + userId;
    }
}

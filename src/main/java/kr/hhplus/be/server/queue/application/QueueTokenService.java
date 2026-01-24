package kr.hhplus.be.server.queue.application;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import kr.hhplus.be.server.queue.domain.QueueStatus;
import kr.hhplus.be.server.queue.domain.QueueToken;
import kr.hhplus.be.server.queue.infrastructure.RedisQueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class QueueTokenService {

    private static final int ACTIVE_LIMIT = 100;
    private static final Duration TOKEN_TTL = Duration.ofMinutes(10);

    private final RedisQueueRepository redisQueueRepo;
    private final Clock clock;

    public QueueToken issue(String userId) {
        Instant now = Instant.now(clock);

        var existing = redisQueueRepo.findTokenByUserId(userId);
        if (existing.isPresent()) {
            return statusOf(existing.get(), userId, now);
        }

        String token = UUID.randomUUID().toString();
        redisQueueRepo.saveToken(userId, token, TOKEN_TTL);
        redisQueueRepo.enqueue(token, now);

        return statusOf(token, userId, now);
    }

    public QueueToken me(String token) {
        Instant now = Instant.now(clock);

        String userId = redisQueueRepo.findUserIdByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("invalid or expired token"));

        return statusOf(token, userId, now);
    }

    public void validate(String token) {
        QueueToken me = me(token);
        if (me.status() != QueueStatus.ACTIVE) {
            throw new IllegalStateException("not active in queue");
        }
    }

    public void expire(String token) {
        String userId = redisQueueRepo.findUserIdByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("invalid or expired token"));
        redisQueueRepo.removeToken(token, userId);
    }

    private QueueToken statusOf(String token, String userId, Instant now) {
        long rank = redisQueueRepo.getRank(token);
        if (rank == -1) {
            // ZSET 에서 없어졌거나 TTL로 키가 날아간 케이스
            return new QueueToken(token, userId, QueueStatus.EXPIRED,
                -1, redisQueueRepo.getSize(), now, now);
        }

        long total = redisQueueRepo.getSize();

        QueueStatus status = rank <= ACTIVE_LIMIT ? QueueStatus.ACTIVE : QueueStatus.WAITING;

        Instant expiresAt = now.plus(TOKEN_TTL);

        return new QueueToken(token, userId, status, rank, total, now, expiresAt);
    }
}

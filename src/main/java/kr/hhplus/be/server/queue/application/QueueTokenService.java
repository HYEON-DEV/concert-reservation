package kr.hhplus.be.server.queue.application;

import java.time.Duration;
import java.time.Instant;
import kr.hhplus.be.server.queue.domain.QueueToken;
import kr.hhplus.be.server.queue.infrastructure.QueueTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class QueueTokenService {

    private static final int MAX_ACTIVE_USERS = 100;
    private static final Duration TOKEN_TTL = Duration.ofMinutes(10);

    private final QueueTokenJpaRepository repo;

    @Transactional
    public QueueToken issue(String userId) {
        Instant now = Instant.now();

        long activeCount = repo.countActiveTokens(now);
        if (activeCount >= MAX_ACTIVE_USERS) {
            throw new IllegalStateException("Queue is full");
        }

        QueueToken token = new QueueToken(
            userId, now, now.plus(TOKEN_TTL));

        return repo.save(token);
    }

    @Transactional(readOnly = true)
    public QueueToken validate(String tokenValue) {
        QueueToken token = repo.findByToken(tokenValue)
            .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (token.isExpired(Instant.now())) {
            throw new IllegalStateException("Token is expired");
        }

        return token;
    }

    @Transactional
    public void expire(String tokenValue) {
        QueueToken token = repo.findByToken(tokenValue)
            .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        token.expire();
    }
}

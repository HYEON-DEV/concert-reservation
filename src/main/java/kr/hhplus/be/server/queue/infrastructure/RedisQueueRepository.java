package kr.hhplus.be.server.queue.infrastructure;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.data.redis.core.StringRedisTemplate;

@RequiredArgsConstructor
@Repository
public class RedisQueueRepository {

    // ZSET: score = 발급시각 (epoch milli) -> 순서 보장
    private static final String WAITING_ZSET = "queue:waiting";

    // token -> userId 토큰으로 누구의 토큰인지 찾기
    private static final String TOKEN_USER_KEY_PREFIX = "queue:token:user:";

    // userId -> token (중복 발급 방지)
    private static final String USER_TOKEN_KEY_PREFIX = "queue:user:token:";

    private final StringRedisTemplate redis;

    public Optional<String> findTokenByUserId(String userId) {
        String token = redis.opsForValue().get(USER_TOKEN_KEY_PREFIX + userId);
        return Optional.ofNullable(token);
    }

    public Optional<String> findUserIdByToken(String token) {
        String userId = redis.opsForValue().get(TOKEN_USER_KEY_PREFIX + token);
        return Optional.ofNullable(userId);
    }

    public void saveToken(String userId, String token, Duration ttl) {
        redis.opsForValue().set(TOKEN_USER_KEY_PREFIX + token, userId, ttl);
        redis.opsForValue().set(USER_TOKEN_KEY_PREFIX + userId, token, ttl);
    }

    public void enqueue(String token, Instant now) {
        redis.opsForZSet().add(WAITING_ZSET, token, now.toEpochMilli());
    }

    public long getRank(String token) {
        Long rank0 = redis.opsForZSet().rank(WAITING_ZSET, token);
        if (rank0 == null) return -1;
        return rank0 + 1;
    }

    public long getSize() {
        Long size = redis.opsForZSet().zCard(WAITING_ZSET);
        return size == null ? 0 : size;
    }

    public void removeToken(String token, String userId) {
        redis.opsForZSet().remove(WAITING_ZSET, token);
        redis.delete(TOKEN_USER_KEY_PREFIX + token);
        redis.delete(USER_TOKEN_KEY_PREFIX + userId);
    }
}

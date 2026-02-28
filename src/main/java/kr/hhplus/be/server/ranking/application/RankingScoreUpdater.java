package kr.hhplus.be.server.ranking.application;

import java.time.Clock;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RankingScoreUpdater {

    private static final Duration DAILY_TTL = Duration.ofDays(2);
    private static final Duration WEEKLY_TTL = Duration.ofDays(14);

    private final StringRedisTemplate redisTemplate;
    private final RankingKeyGenerator keyGenerator;
    private final Clock clock;

    public void increasePerformanceScore(Long performanceId, double amount) {
        String member = String.valueOf(performanceId);

        String dailyKey = keyGenerator.dailyKey(clock);
        redisTemplate.opsForZSet().incrementScore(dailyKey, member, amount);
        ensureTtl(dailyKey, DAILY_TTL);

        String weeklyKey = keyGenerator.weeklyKey(clock);
        redisTemplate.opsForZSet().incrementScore(weeklyKey, member, amount);
        ensureTtl(weeklyKey, WEEKLY_TTL);
    }

    private void ensureTtl(String key, Duration ttl) {
        Long current = redisTemplate.getExpire(key);
        if (current == null || current < 0) {
            redisTemplate.expire(key, ttl);
        }
    }
}

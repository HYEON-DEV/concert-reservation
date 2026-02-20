package kr.hhplus.be.server.ranking.application;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RankingEventPublisher {

    public static final String CHANNEL = "ranking:events:payment";

    private final StringRedisTemplate redisTemplate;

    public void publishPaymentCompleted(Long performanceId, Instant paidAt) {
        String payload = performanceId + "|" + paidAt.toEpochMilli();
        redisTemplate.convertAndSend(CHANNEL, payload);
    }
}

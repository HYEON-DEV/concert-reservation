package kr.hhplus.be.server.concert.infrastructure;

import java.time.Duration;
import java.util.Objects;
import kr.hhplus.be.server.concert.application.port.SeatHoldPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisSeatHoldAdapter implements SeatHoldPort {

    private final StringRedisTemplate redisTemplate;

    private String key(Long performanceId, int seatNo) {
        return "seat:hold:" + performanceId + ":" + seatNo;
    }

    @Override
    public boolean hold(Long performanceId, int seatNo, String userId, Duration ttl) {
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(key(performanceId, seatNo), userId, ttl);
        return Boolean.TRUE.equals(ok);
    }

    @Override
    public boolean isHeldBy(Long performanceId, int seatNo, String userId) {
        String v = redisTemplate.opsForValue().get(key(performanceId, seatNo));
        return Objects.equals(v, userId);
    }

    @Override
    public void release(Long performanceId, int seatNo, String userId) {
        String k = key(performanceId, seatNo);
        String v = redisTemplate.opsForValue().get(k);
        if (Objects.equals(v, userId)) {
            redisTemplate.delete(k);
        }
    }
}

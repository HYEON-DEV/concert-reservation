package kr.hhplus.be.server.ranking.application;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RankingQueryService {

    private final StringRedisTemplate redisTemplate;
    private final RankingKeyGenerator keyGenerator;
    private final Clock clock;

    public List<RankingItem> topDaily(int limit) {
        String key = keyGenerator.dailyKey(clock);
        return readTop(key, limit);
    }

    public List<RankingItem> topWeekly(int limit) {
        String key = keyGenerator.weeklyKey(clock);
        return readTop(key, limit);
    }

    private List<RankingItem> readTop(String key, int limit) {
        Set<TypedTuple<String>> tuples = redisTemplate.opsForZSet()
            .reverseRangeWithScores(key, 0, Math.max(limit - 1, 0));

        if (tuples == null || tuples.isEmpty()) {
            return List.of();
        }

        List<RankingItem> result = new ArrayList<>();
        int rank = 1;
        for (TypedTuple<String> tuple : tuples) {
            if (tuple.getValue() == null || tuple.getScore() == null) {
                continue;
            }
            result.add(new RankingItem(rank++, Long.parseLong(tuple.getValue()), tuple.getScore()));
        }
        return result;
    }

    public record RankingItem(int rank, Long performanceId, Double score) {
    }
}

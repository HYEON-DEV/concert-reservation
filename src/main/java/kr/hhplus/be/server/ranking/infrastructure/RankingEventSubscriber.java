package kr.hhplus.be.server.ranking.infrastructure;

import kr.hhplus.be.server.ranking.application.RankingScoreUpdater;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingEventSubscriber implements MessageListener {

    private final RankingScoreUpdater rankingScoreUpdater;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String payload = new String(message.getBody());
        try {
            String[] tokens = payload.split("\\|");
            Long performanceId = Long.parseLong(tokens[0]);
            rankingScoreUpdater.increasePerformanceScore(performanceId, 1.0d);
        } catch (Exception e) {
            log.warn("failed to handle ranking event payload={}", payload, e);
        }
    }
}

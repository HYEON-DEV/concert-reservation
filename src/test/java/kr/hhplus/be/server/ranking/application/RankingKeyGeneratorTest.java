package kr.hhplus.be.server.ranking.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RankingKeyGeneratorTest {

    private final RankingKeyGenerator keyGenerator = new RankingKeyGenerator();

    @Test
    @DisplayName("UTC 기준 일간/주간 키를 생성한다")
    void generateDailyWeeklyKey() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-20T10:00:00Z"), ZoneOffset.UTC);

        assertEquals("ranking:concert:daily:20260220", keyGenerator.dailyKey(clock));
        assertEquals("ranking:concert:weekly:2026-W08", keyGenerator.weeklyKey(clock));
    }
}

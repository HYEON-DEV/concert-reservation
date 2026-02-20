package kr.hhplus.be.server.ranking.application;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class RankingKeyGenerator {

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.BASIC_ISO_DATE;

    public String dailyKey(Clock clock) {
        LocalDate now = LocalDate.now(clock.withZone(ZoneOffset.UTC));
        return "ranking:concert:daily:" + DAY_FMT.format(now);
    }

    public String weeklyKey(Clock clock) {
        LocalDate now = LocalDate.now(clock.withZone(ZoneOffset.UTC));
        WeekFields wf = WeekFields.of(Locale.US);
        int year = now.get(wf.weekBasedYear());
        int week = now.get(wf.weekOfWeekBasedYear());
        return "ranking:concert:weekly:%d-W%02d".formatted(year, week);
    }
}

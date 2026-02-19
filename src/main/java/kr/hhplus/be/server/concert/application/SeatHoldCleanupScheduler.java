package kr.hhplus.be.server.concert.application;

import java.time.Clock;
import java.time.Instant;
import kr.hhplus.be.server.concert.infrastructure.ConcertSeatJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatHoldCleanupScheduler {

    private final ConcertSeatJpaRepository seatRepository;
    private final Clock clock;

    @Transactional
    @Scheduled(fixedDelayString = "${app.seat-hold.cleanup-delay-ms:10000}")
    public void releaseExpiredHolds() {
        Instant now = Instant.now(clock);
        int released = seatRepository.releaseExpiredHolds(now);
        if (released > 0) {
            log.info("released {} expired seat holds at {}", released, now);
        }
    }
}

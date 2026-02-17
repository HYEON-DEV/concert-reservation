package kr.hhplus.be.server.concert.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import kr.hhplus.be.server.concert.infrastructure.ConcertSeatJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class SeatHoldCleanupSchedulerTest {

    @Mock
    ConcertSeatJpaRepository seatRepository;

    private SeatHoldCleanupScheduler scheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Clock fixedClock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        scheduler = new SeatHoldCleanupScheduler(seatRepository, fixedClock);
    }

    @Test
    @DisplayName("만료된 HOLD 좌석이 있으면 스케줄러가 해제 UPDATE를 호출한다")
    void releaseExpiredHolds_executesBulkUpdate() {
        when(seatRepository.releaseExpiredHolds(any())).thenReturn(3);

        scheduler.releaseExpiredHolds();

        verify(seatRepository, times(1)).releaseExpiredHolds(any());
    }
}

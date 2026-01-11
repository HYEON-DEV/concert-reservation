package kr.hhplus.be.server.concert.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import kr.hhplus.be.server.concert.domain.ConcertPerformance;
import kr.hhplus.be.server.concert.domain.ConcertSeat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.concert.infrastructure.ConcertPerformanceJpaRepository;
import kr.hhplus.be.server.concert.infrastructure.ConcertSeatJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ConcertServiceTest {

    @Mock
    ConcertPerformanceJpaRepository performanceRepo;

    @Mock
    ConcertSeatJpaRepository seatRepo;

    @InjectMocks
    ConcertService concertService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("공연 목록 조회: concertId로 공연을 startAt 오름차순으로 조회하고 DTO로 변환한다")
    void getPerformances_mapsToDto() {
        Long concertId = 1L;
        Instant t1 = Instant.parse("2026-01-01T10:00:00Z");
        Instant t2 = Instant.parse("2026-01-02T12:00:00Z");

        ConcertPerformance p1 = mock(ConcertPerformance.class);
        when(p1.id()).thenReturn(101L);
        when(p1.startAt()).thenReturn(t1);

        ConcertPerformance p2 = mock(ConcertPerformance.class);
        when(p2.id()).thenReturn(102L);
        when(p2.startAt()).thenReturn(t2);

        when(performanceRepo.findAllByConcertIdOrderByStartAtAsc(concertId))
            .thenReturn(List.of(p1,p2));

        List<ConcertService.PerformanceDto> result = concertService.getPerformances(concertId);

        assertEquals(2, result.size());

        assertEquals(101L, result.get(0).performanceId());
        assertEquals(t1, result.get(0).startAt());

        assertEquals(102L, result.get(1).performanceId());
        assertEquals(t2, result.get(1).startAt());

        verify(performanceRepo).findAllByConcertIdOrderByStartAtAsc(concertId);
        verifyNoMoreInteractions(performanceRepo, seatRepo);
    }

    @Test
    @DisplayName("좌석 목록 조회: performanceId로 좌석을 seatNo 오름차순으로 조회하고 DTO로 변환한다")
    void getSeats_mapsToDto() {
        Long performanceId = 10L;
        ConcertSeat s1 = new ConcertSeat(performanceId, 1, SeatStatus.AVAILABLE);
        ConcertSeat s2 = new ConcertSeat(performanceId, 2, SeatStatus.HOLD);

        when(seatRepo.findAllByPerformanceIdOrderBySeatNoAsc(performanceId))
            .thenReturn(List.of(s1,s2));

        List<ConcertService.SeatDto> result = concertService.getSeats(performanceId);

        assertEquals(2, result.size());

        assertEquals(1, result.get(0).seatNo());
        assertEquals("AVAILABLE", result.get(0).status());

        assertEquals(2, result.get(1).seatNo());
        assertEquals("HOLD", result.get(1).status());

        verify(seatRepo).findAllByPerformanceIdOrderBySeatNoAsc(performanceId);
        verifyNoMoreInteractions(performanceRepo, seatRepo);
    }

    @Test
    @DisplayName("공연 목록 조회: 조회 결과가 없으면 빈 리스트를 반환한다")
    void getPerformances_returnsEmptyList_whenNoData() {
        Long concertId = 999L;
        when(performanceRepo.findAllByConcertIdOrderByStartAtAsc(concertId))
            .thenReturn(List.of());

        List<ConcertService.PerformanceDto> result = concertService.getPerformances(concertId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(performanceRepo).findAllByConcertIdOrderByStartAtAsc(concertId);
        verifyNoMoreInteractions(performanceRepo, seatRepo);
    }

    @Test
    @DisplayName("좌석 목록 조회: 조회 결과가 없으면 빈 리스트를 반환한다")
    void getSeats_returnsEmptyList_whenNoData() {
        Long performanceId = 999L;

        when(seatRepo.findAllByPerformanceIdOrderBySeatNoAsc(performanceId))
            .thenReturn(List.of());

        List<ConcertService.SeatDto> result = concertService.getSeats(performanceId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(seatRepo).findAllByPerformanceIdOrderBySeatNoAsc(performanceId);
        verifyNoMoreInteractions(performanceRepo, seatRepo);
    }
}

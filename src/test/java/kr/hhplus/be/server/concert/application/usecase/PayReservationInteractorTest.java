package kr.hhplus.be.server.concert.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import kr.hhplus.be.server.concert.application.port.PaymentPort;
import kr.hhplus.be.server.concert.application.port.PointPort;
import kr.hhplus.be.server.concert.application.port.SeatHoldPort;
import kr.hhplus.be.server.concert.application.port.SeatPort;
import kr.hhplus.be.server.concert.application.usecase.PayReservationUseCase.Command;
import kr.hhplus.be.server.concert.domain.ConcertSeat;
import kr.hhplus.be.server.ranking.application.RankingEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PayReservationInteractorTest {

    @Mock SeatPort seatPort;
    @Mock PointPort pointPort;
    @Mock PaymentPort paymentPort;
    @Mock SeatHoldPort seatHoldPort;
    @Mock RankingEventPublisher rankingEventPublisher;

    Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    PayReservationInteractor interactor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        interactor = new PayReservationInteractor(
            seatPort, pointPort, paymentPort, seatHoldPort, rankingEventPublisher, clock);
    }

    @Test
    @DisplayName("성공: Redis owner + DB HOLD + 결제/포인트 + RESERVED + Redis release")
    void pay_success() {
        // given
        ConcertSeat seat = new ConcertSeat(1L, 1);
        seat.hold("u1", Instant.parse("2026-01-01T00:10:00Z"));

        when(seatHoldPort.isHeldBy(1L, 1, "u1")).thenReturn(true);
        when(seatPort.findForUpdate(1L, 1)).thenReturn(Optional.of(seat));
        when(seatPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // when
        var result = interactor.pay(new Command("u1", 1L, 1, 1000));

        // then
        assertEquals(1L, result.performanceId());
        assertEquals(1, result.seatNo());
        assertEquals("RESERVED", result.status());

        verify(pointPort).usePoint("u1", 1000);
        verify(paymentPort).record(eq("u1"), eq(1L), eq(1), eq(1000L), any());
        verify(rankingEventPublisher).publishPaymentCompleted(eq(1L), any());
        verify(seatPort).save(seat);
        verify(seatHoldPort).release(1L, 1, "u1");
    }

    @Test
    @DisplayName("실패: Redis owner 아님(만료/타유저) -> DB 조회도 안 함")
    void pay_fail_not_owner() {
        when(seatHoldPort.isHeldBy(1L, 1, "u1")).thenReturn(false);

        assertThrows(IllegalStateException.class,
            () -> interactor.pay(new PayReservationUseCase.Command("u1", 1L, 1, 1000)));

        verify(seatPort, never()).findForUpdate(anyLong(), anyInt());
        verify(pointPort, never()).usePoint(any(), anyLong());
        verify(paymentPort, never()).record(any(), any(), anyInt(), anyLong(), any());
    }

    @Test
    @DisplayName("실패: 좌석 없음")
    void pay_fail_seat_not_found() {
        when(seatHoldPort.isHeldBy(1L, 1, "u1")).thenReturn(true);
        when(seatPort.findForUpdate(1L, 1)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> interactor.pay(new PayReservationUseCase.Command("u1", 1L, 1, 1000)));

        verify(pointPort, never()).usePoint(any(), anyLong());
        verify(paymentPort, never()).record(any(), any(), anyInt(), anyLong(), any());
        verify(seatPort, never()).save(any());
    }

    @Test
    @DisplayName("실패: DB HOLD 만료 -> release + save + Redis release 시도")
    void pay_fail_hold_expired() {
        ConcertSeat seat = new ConcertSeat(1L, 1);
        seat.hold("u1", Instant.parse("2025-12-31T23:00:00Z")); // 만료

        when(seatHoldPort.isHeldBy(1L, 1, "u1")).thenReturn(true);
        when(seatPort.findForUpdate(1L, 1)).thenReturn(Optional.of(seat));
        when(seatPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThrows(IllegalStateException.class,
            () -> interactor.pay(new PayReservationUseCase.Command("u1", 1L, 1, 1000)));

        verify(seatPort).save(seat);
        verify(seatHoldPort).release(1L, 1, "u1");
        verify(pointPort, never()).usePoint(any(), anyLong());
        verify(paymentPort, never()).record(any(), any(), anyInt(), anyLong(), any());
    }
}

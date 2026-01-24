package kr.hhplus.be.server.concert.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.*;
import java.util.Optional;
import kr.hhplus.be.server.concert.application.port.*;
import kr.hhplus.be.server.concert.domain.ConcertSeat;
import org.junit.jupiter.api.*;
import org.mockito.*;

class PayReservationInteractorTest {

    @Mock SeatPort seatPort;
    @Mock PointPort pointPort;
    @Mock PaymentPort paymentPort;

    Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    PayReservationInteractor interactor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        interactor = new PayReservationInteractor(seatPort, pointPort, paymentPort, clock);
    }

    @Test
    @DisplayName("정상 결제 -> 포인트 차감, 결제 기록, 좌석 RESERVED")
    void pay_success() {
        ConcertSeat seat = new ConcertSeat(1L, 1);
        seat.hold("u1", Instant.parse("2026-01-01T00:10:00Z"));

        when(seatPort.findForUpdate(1L, 1)).thenReturn(Optional.of(seat));
        when(seatPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = interactor.pay(new PayReservationUseCase.Command("u1", 1L, 1, 1000));

        assertEquals("RESERVED", result.status());
        verify(pointPort).usePoint("u1", 1000);
        verify(paymentPort).record(eq("u1"), eq(1L), eq(1), eq(1000L), any());
        verify(seatPort).save(seat);
    }

    @Test
    @DisplayName("다른 유저가 HOLD한 좌석은 결제 불가")
    void pay_not_holder_fail() {
        ConcertSeat seat = new ConcertSeat(1L, 1);
        seat.hold("u2", Instant.parse("2026-01-01T00:10:00Z"));

        when(seatPort.findForUpdate(1L, 1)).thenReturn(Optional.of(seat));

        assertThrows(IllegalStateException.class,
            () -> interactor.pay(new PayReservationUseCase.Command("u1", 1L, 1, 1000)));

        verify(pointPort, never()).usePoint(any(), anyLong());
        verify(paymentPort, never()).record(any(), any(), anyInt(), anyLong(), any());
    }

    @Test
    @DisplayName("HOLD 만료면 결제 불가 + 좌석 release")
    void pay_hold_expired_fail() {
        ConcertSeat seat = new ConcertSeat(1L, 1);
        seat.hold("u1", Instant.parse("2025-12-31T23:00:00Z")); // 만료

        when(seatPort.findForUpdate(1L, 1)).thenReturn(Optional.of(seat));
        when(seatPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThrows(IllegalStateException.class,
            () -> interactor.pay(new PayReservationUseCase.Command("u1", 1L, 1, 1000)));

        // 만료로 release -> save 호출됨
        verify(seatPort).save(seat);
        verify(pointPort, never()).usePoint(any(), anyLong());
        verify(paymentPort, never()).record(any(), any(), anyInt(), anyLong(), any());
    }
}
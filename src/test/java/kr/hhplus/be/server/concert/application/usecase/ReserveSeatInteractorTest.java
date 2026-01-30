package kr.hhplus.be.server.concert.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.*;
import java.util.Optional;
import kr.hhplus.be.server.concert.application.port.SeatHoldPort;
import kr.hhplus.be.server.concert.application.port.SeatPort;
import kr.hhplus.be.server.concert.domain.ConcertSeat;
import org.junit.jupiter.api.*;
import org.mockito.*;

class ReserveSeatInteractorTest {

    @Mock SeatPort seatPort;
    @Mock SeatHoldPort seatHoldPort;

    Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    @InjectMocks ReserveSeatInteractor interactor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        interactor = new ReserveSeatInteractor(seatPort, seatHoldPort, clock);
    }

    @Test
    @DisplayName("AVAILABLE 좌석은 HOLD 성공")
    void reserve_available_success() {
        ConcertSeat seat = new ConcertSeat(1L, 1);
        when(seatPort.findForUpdate(1L, 1)).thenReturn(Optional.of(seat));
        when(seatPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = interactor.reserve(new ReserveSeatUseCase.Command("u1", 1L, 1));

        assertEquals("HOLD", result.status());
        assertEquals("u1", seat.holdUserId());
        verify(seatPort).save(seat);
    }

    @Test
    @DisplayName("다른 유저가 HOLD 중이면 실패")
    void reserve_held_by_other_fail() {
        ConcertSeat seat = new ConcertSeat(1L, 1);
        seat.hold("u2", Instant.parse("2026-01-01T00:10:00Z"));

        when(seatPort.findForUpdate(1L, 1)).thenReturn(Optional.of(seat));

        assertThrows(IllegalStateException.class,
            () -> interactor.reserve(new ReserveSeatUseCase.Command("u1", 1L, 1)));
    }

    @Test
    @DisplayName("HOLD 만료면 release 후 재HOLD 가능")
    void reserve_expired_hold_can_take() {
        ConcertSeat seat = new ConcertSeat(1L, 1);
        seat.hold("u2", Instant.parse("2025-12-31T23:00:00Z")); // 이미 만료

        when(seatPort.findForUpdate(1L, 1)).thenReturn(Optional.of(seat));
        when(seatPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = interactor.reserve(new ReserveSeatUseCase.Command("u1", 1L, 1));

        assertEquals("HOLD", result.status());
        assertEquals("u1", seat.holdUserId());
        verify(seatPort).save(seat);
    }
}

package kr.hhplus.be.server.concert.application;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;

import java.time.Instant;
import kr.hhplus.be.server.concert.application.event.ReservationCompletedEvent;
import kr.hhplus.be.server.concert.application.port.ReservationMessagePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ReservationCompletedEventListenerTest {

    @Mock
    ReservationMessagePort reservationMessagePort;

    private ReservationCompletedEventListener listener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        listener = new ReservationCompletedEventListener(reservationMessagePort);
    }

    @Test
    @DisplayName("결제 완료 이벤트를 수신하면 Kafka 발행 메시지로 변환한다")
    void handle() {
        ReservationCompletedEvent event =
            new ReservationCompletedEvent("u1", 1L, 1, 1000L, Instant.parse("2026-01-01T00:00:00Z"));

        listener.handle(event);

        verify(reservationMessagePort).publishReservationCompleted(any());
    }
}

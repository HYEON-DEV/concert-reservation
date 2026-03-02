package kr.hhplus.be.server.concert.application;

import static org.mockito.Mockito.verify;

import java.time.Instant;
import kr.hhplus.be.server.concert.application.event.ReservationCompletedEvent;
import kr.hhplus.be.server.concert.application.port.ReservationDataPlatformPort;
import kr.hhplus.be.server.ranking.application.RankingEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ReservationCompletedEventListenerTest {

    @Mock
    ReservationDataPlatformPort reservationDataPlatformPort;

    @Mock
    RankingEventPublisher rankingEventPublisher;

    private ReservationCompletedEventListener listener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        listener = new ReservationCompletedEventListener(reservationDataPlatformPort, rankingEventPublisher);
    }

    @Test
    @DisplayName("결제 완료 이벤트를 수신하면 mock API 전송과 랭킹 후속 처리를 수행한다")
    void handle() {
        ReservationCompletedEvent event =
            new ReservationCompletedEvent("u1", 1L, 1, 1000L, Instant.parse("2026-01-01T00:00:00Z"));

        listener.handle(event);

        verify(reservationDataPlatformPort).sendReservationCompleted(event);
        verify(rankingEventPublisher).publishPaymentCompleted(1L, event.paidAt());
    }
}

package kr.hhplus.be.server.concert.infrastructure;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import kr.hhplus.be.server.concert.application.event.ReservationCompletedMessage;
import kr.hhplus.be.server.concert.application.port.ReservationDataPlatformPort;
import kr.hhplus.be.server.ranking.application.RankingEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.support.Acknowledgment;

class KafkaReservationCompletedConsumerTest {

    @Mock
    ReservationDataPlatformPort reservationDataPlatformPort;

    @Mock
    RankingEventPublisher rankingEventPublisher;

    @Mock
    Acknowledgment acknowledgment;

    private KafkaReservationCompletedConsumer consumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        consumer = new KafkaReservationCompletedConsumer(reservationDataPlatformPort, rankingEventPublisher);
    }

    @Test
    @DisplayName("Kafka 메시지를 소비하면 mock API 전송 후 ack 한다")
    void consume() {
        ReservationCompletedMessage message = new ReservationCompletedMessage(
            "event-1",
            "u1",
            1L,
            1,
            1000L,
            Instant.parse("2026-01-01T00:00:00Z")
        );

        consumer.consume(message, acknowledgment);

        verify(reservationDataPlatformPort).sendReservationCompleted(any());
        verify(rankingEventPublisher).publishPaymentCompleted(1L, message.paidAt());
        verify(acknowledgment).acknowledge();
    }
}

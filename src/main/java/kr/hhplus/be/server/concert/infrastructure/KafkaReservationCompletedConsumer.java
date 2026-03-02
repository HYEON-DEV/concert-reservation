package kr.hhplus.be.server.concert.infrastructure;

import kr.hhplus.be.server.concert.application.event.ReservationCompletedEvent;
import kr.hhplus.be.server.concert.application.event.ReservationCompletedMessage;
import kr.hhplus.be.server.concert.application.port.ReservationDataPlatformPort;
import kr.hhplus.be.server.ranking.application.RankingEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaReservationCompletedConsumer {

    private final ReservationDataPlatformPort reservationDataPlatformPort;
    private final RankingEventPublisher rankingEventPublisher;

    @KafkaListener(
        topics = "${app.kafka.topics.reservation-completed}",
        groupId = "concert-data-platform"
    )
    public void consume(ReservationCompletedMessage message, Acknowledgment acknowledgment) {
        try {
            ReservationCompletedEvent event = new ReservationCompletedEvent(
                message.userId(),
                message.performanceId(),
                message.seatNo(),
                message.amount(),
                message.paidAt()
            );
            reservationDataPlatformPort.sendReservationCompleted(event);
            rankingEventPublisher.publishPaymentCompleted(message.performanceId(), message.paidAt());
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.warn("failed to consume reservation completed message eventId={}", message.eventId(), e);
            throw e;
        }
    }
}

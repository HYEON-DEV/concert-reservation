package kr.hhplus.be.server.concert.infrastructure;

import kr.hhplus.be.server.concert.application.event.ReservationCompletedMessage;
import kr.hhplus.be.server.concert.application.port.ReservationMessagePort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaReservationMessageProducer implements ReservationMessagePort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.reservation-completed}")
    private String reservationCompletedTopic;

    @Override
    public void publishReservationCompleted(ReservationCompletedMessage message) {
        kafkaTemplate.send(reservationCompletedTopic, String.valueOf(message.performanceId()), message);
    }
}

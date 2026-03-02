package kr.hhplus.be.server.concert.infrastructure;

import kr.hhplus.be.server.concert.application.event.ReservationCompletedEvent;
import kr.hhplus.be.server.concert.application.port.ReservationEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringReservationEventPublisher implements ReservationEventPort {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishReservationCompleted(ReservationCompletedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}

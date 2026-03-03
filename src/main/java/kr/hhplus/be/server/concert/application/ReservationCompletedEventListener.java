package kr.hhplus.be.server.concert.application;

import kr.hhplus.be.server.concert.application.event.ReservationCompletedEvent;
import kr.hhplus.be.server.concert.application.event.ReservationCompletedMessage;
import kr.hhplus.be.server.concert.application.port.ReservationMessagePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReservationCompletedEventListener {

    private final ReservationMessagePort reservationMessagePort;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ReservationCompletedEvent event) {
        reservationMessagePort.publishReservationCompleted(ReservationCompletedMessage.from(event));
    }
}

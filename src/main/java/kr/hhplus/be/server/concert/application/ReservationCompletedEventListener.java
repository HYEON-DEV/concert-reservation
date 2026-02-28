package kr.hhplus.be.server.concert.application;

import kr.hhplus.be.server.concert.application.event.ReservationCompletedEvent;
import kr.hhplus.be.server.concert.application.port.ReservationDataPlatformPort;
import kr.hhplus.be.server.ranking.application.RankingEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationCompletedEventListener {

    private final ReservationDataPlatformPort reservationDataPlatformPort;
    private final RankingEventPublisher rankingEventPublisher;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ReservationCompletedEvent event) {
        try {
            reservationDataPlatformPort.sendReservationCompleted(event);
        } catch (Exception e) {
            log.warn("reservation completed side-effect failed: data-platform send", e);
        }

        try {
            rankingEventPublisher.publishPaymentCompleted(event.performanceId(), event.paidAt());
        } catch (Exception e) {
            log.warn("reservation completed side-effect failed: ranking publish", e);
        }
    }
}

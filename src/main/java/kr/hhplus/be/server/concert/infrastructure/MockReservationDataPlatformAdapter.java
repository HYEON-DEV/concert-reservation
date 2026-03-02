package kr.hhplus.be.server.concert.infrastructure;

import kr.hhplus.be.server.concert.application.event.ReservationCompletedEvent;
import kr.hhplus.be.server.concert.application.port.ReservationDataPlatformPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockReservationDataPlatformAdapter implements ReservationDataPlatformPort {

    @Override
    public void sendReservationCompleted(ReservationCompletedEvent event) {
        log.info(
            "mock data-platform send reservation completed userId={}, performanceId={}, seatNo={}, amount={}, paidAt={}",
            event.userId(),
            event.performanceId(),
            event.seatNo(),
            event.amount(),
            event.paidAt()
        );
    }
}

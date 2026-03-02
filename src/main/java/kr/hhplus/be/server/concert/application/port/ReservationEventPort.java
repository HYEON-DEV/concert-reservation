package kr.hhplus.be.server.concert.application.port;

import kr.hhplus.be.server.concert.application.event.ReservationCompletedEvent;

public interface ReservationEventPort {

    void publishReservationCompleted(ReservationCompletedEvent event);
}

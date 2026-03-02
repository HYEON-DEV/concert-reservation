package kr.hhplus.be.server.concert.application.port;

import kr.hhplus.be.server.concert.application.event.ReservationCompletedEvent;

public interface ReservationDataPlatformPort {

    void sendReservationCompleted(ReservationCompletedEvent event);
}

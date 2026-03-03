package kr.hhplus.be.server.concert.application.port;

import kr.hhplus.be.server.concert.application.event.ReservationCompletedMessage;

public interface ReservationMessagePort {

    void publishReservationCompleted(ReservationCompletedMessage message);
}

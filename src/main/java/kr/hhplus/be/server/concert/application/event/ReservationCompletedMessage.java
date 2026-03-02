package kr.hhplus.be.server.concert.application.event;

import java.time.Instant;

public record ReservationCompletedMessage(
    String eventId,
    String userId,
    Long performanceId,
    int seatNo,
    long amount,
    Instant paidAt
) {
    public static ReservationCompletedMessage from(ReservationCompletedEvent event) {
        String eventId = "reservation-completed:%s:%d:%d:%s".formatted(
            event.userId(),
            event.performanceId(),
            event.seatNo(),
            event.paidAt().toEpochMilli()
        );
        return new ReservationCompletedMessage(
            eventId,
            event.userId(),
            event.performanceId(),
            event.seatNo(),
            event.amount(),
            event.paidAt()
        );
    }
}

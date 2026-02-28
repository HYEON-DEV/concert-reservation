package kr.hhplus.be.server.concert.application.event;

import java.time.Instant;

public record ReservationCompletedEvent(
    String userId,
    Long performanceId,
    int seatNo,
    long amount,
    Instant paidAt
) {
}

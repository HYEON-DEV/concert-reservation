package kr.hhplus.be.server.concert.application.port;

import java.time.Instant;

public interface PaymentPort {
    void record(String userId, Long performanceId, int seatNo, long amount, Instant paidAt);
}

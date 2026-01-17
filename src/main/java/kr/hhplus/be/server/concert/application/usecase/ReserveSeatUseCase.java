package kr.hhplus.be.server.concert.application.usecase;

import java.time.Instant;

public interface ReserveSeatUseCase {

    Result reserve(Command command);

    record Command(String userId, Long performanceId, int seatNo) {}

    record Result(Long performanceId, int seatNo, String status, Instant holdUntil) {}
}

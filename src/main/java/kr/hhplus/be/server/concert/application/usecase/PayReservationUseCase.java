package kr.hhplus.be.server.concert.application.usecase;

public interface PayReservationUseCase {

    Result pay(Command command);

    record Command(String userId, Long performanceId, int seatNo, long amount) {}

    record Result(Long performanceId, int seatNo, String status) {}
}

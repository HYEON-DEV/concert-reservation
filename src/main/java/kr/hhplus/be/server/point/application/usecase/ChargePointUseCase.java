package kr.hhplus.be.server.point.application.usecase;

public interface ChargePointUseCase {

    long charge(String userId, long amount);
}

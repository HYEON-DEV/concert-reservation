package kr.hhplus.be.server.concert.application.port;

import java.time.Duration;

public interface SeatHoldPort {

    // 좌석 HOLD 선점 (동시에 여러명 요청해도 1명만 성공)
    boolean hold(Long performanceId, int seatNo, String userId, Duration ttl);

    // 결제 시점에 내가 잡은 HOLD가 맞는지 확인
    boolean isHeldBy(Long performanceId, int seatNo, String userId);

    // HOLD 해제 (결제 완료/취소/만료 처리용)
    void release(Long performanceId, int seatNo, String userId);
}

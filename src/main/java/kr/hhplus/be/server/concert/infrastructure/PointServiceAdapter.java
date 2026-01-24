package kr.hhplus.be.server.concert.infrastructure;

import kr.hhplus.be.server.concert.application.port.PointPort;
import kr.hhplus.be.server.point.application.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PointServiceAdapter implements PointPort {

    private final PointService pointService;

    @Override
    public void usePoint(String userId, long amount) {
        pointService.use(userId, amount);
    }
}

package kr.hhplus.be.server.concert.infrastructure;

import kr.hhplus.be.server.concert.application.port.PointPort;
import kr.hhplus.be.server.point.application.usecase.UsePointUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PointServiceAdapter implements PointPort {

    private final UsePointUseCase usePointUseCase;

    @Override
    public void usePoint(String userId, long amount) {
        usePointUseCase.use(userId, amount);
    }
}

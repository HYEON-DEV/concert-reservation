package kr.hhplus.be.server.concert.infrastructure;

import kr.hhplus.be.server.concert.application.port.QueueTokenPort;
import kr.hhplus.be.server.queue.application.QueueTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class QueueTokenServiceAdapter implements QueueTokenPort {

    private final QueueTokenService queueTokenService;

    @Override
    public void validate(String token, String userId) {
        queueTokenService.validate(token);
    }

    @Override
    public void expire(String token) {
        queueTokenService.expire(token);
    }
}

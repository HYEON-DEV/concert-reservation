package kr.hhplus.be.server.concert.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import kr.hhplus.be.server.concert.application.port.SeatHoldPort;
import kr.hhplus.be.server.concert.application.port.SeatPort;
import kr.hhplus.be.server.concert.domain.ConcertSeat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ReserveSeatInteractor implements ReserveSeatUseCase{

    private static final Duration HOLD_DURATION = Duration.ofMinutes(5);

    private final SeatPort seatPort;
    private final SeatHoldPort seatHoldPort;
    private final Clock clock;


    @Transactional
    @Override
    public Result reserve(Command command) {
        // Redis 선점 (동시성 차단)
        boolean locked = seatHoldPort.hold(
            command.performanceId(), command.seatNo(), command.userId(), HOLD_DURATION
        );

        if (!locked && !seatHoldPort.isHeldBy(command.performanceId(), command.seatNo(),  command.userId())) {
            throw new IllegalStateException("seat is currently held by another user.");
        }

        ConcertSeat seat = seatPort.findForUpdate(command.performanceId(), command.seatNo())
            .orElseThrow(() -> new IllegalArgumentException("seat not found"));

        Instant now = Instant.now(clock);

        // 만료면 풀어주고 재시도 허용
        seat.releaseIfExpired(now);

        // 이미 다른 사람이 잡고 있으면 실패
        if (seat.status() == SeatStatus.HOLD && !command.userId().equals(seat.holdUserId())) {
            seatHoldPort.release(command.performanceId(), command.seatNo(), command.userId());
            throw new IllegalStateException("seat is currently held by another user");
        }

        Instant holdUntil = now.plus(HOLD_DURATION);
        seat.hold(command.userId(), holdUntil);
        seatPort.save(seat);

        return new Result(seat.performanceId(), seat.seatNo(), seat.status().name(), seat.holdUntil());
    }
}

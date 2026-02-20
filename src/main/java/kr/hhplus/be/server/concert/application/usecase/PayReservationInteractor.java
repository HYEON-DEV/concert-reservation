package kr.hhplus.be.server.concert.application.usecase;

import java.time.Clock;
import java.time.Instant;
import kr.hhplus.be.server.concert.application.port.PaymentPort;
import kr.hhplus.be.server.concert.application.port.PointPort;
import kr.hhplus.be.server.concert.application.port.SeatHoldPort;
import kr.hhplus.be.server.concert.application.port.SeatPort;
import kr.hhplus.be.server.concert.domain.ConcertSeat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.ranking.application.RankingEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PayReservationInteractor implements PayReservationUseCase{
    private final SeatPort seatPort;
    private final PointPort pointPort;
    private final PaymentPort paymentPort;
    private final SeatHoldPort seatHoldPort;
    private final RankingEventPublisher rankingEventPublisher;
    private final Clock clock;

    @Transactional
    @CacheEvict(cacheNames = "concert:seats", key = "#command.performanceId()")
    @Override
    public Result pay(Command command) {
        if (command.amount() <= 0) throw new IllegalArgumentException("amount must be positive");

        if (!seatHoldPort.isHeldBy(command.performanceId(), command.seatNo(), command.userId())) {
            throw new IllegalStateException("hold expired or now owner.");
        }

        ConcertSeat seat = seatPort.findForUpdate(command.performanceId(), command.seatNo())
            .orElseThrow(() -> new IllegalArgumentException("seat not found"));

        Instant now = Instant.now(clock);

        // 만료면 해제 후 실패
        seat.releaseIfExpired(now);
        if (seat.status() != SeatStatus.HOLD) {
            seatPort.save(seat);
            seatHoldPort.release(command.performanceId(), command.seatNo(), command.userId());
            throw new IllegalStateException("seat is not held");
        }

        if (!command.userId().equals(seat.holdUserId())) {
            throw new IllegalStateException("not seat holder");
        }

        pointPort.usePoint(command.userId(), command.amount());

        paymentPort.record(
            command.userId(), command.performanceId(), command.seatNo(), command.amount(), now);

        seat.reserve(command.userId());
        seatPort.save(seat);
        seatHoldPort.release(command.performanceId(), command.seatNo(), command.userId());
        rankingEventPublisher.publishPaymentCompleted(command.performanceId(), now);

        return new Result(seat.performanceId(), seat.seatNo(), seat.status().name());
    }
}

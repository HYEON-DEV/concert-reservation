package kr.hhplus.be.server.concert.infrastructure;

import java.util.Optional;
import kr.hhplus.be.server.concert.application.port.SeatPort;
import kr.hhplus.be.server.concert.domain.ConcertSeat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SeatJpaAdapter implements SeatPort {

    private final ConcertSeatJpaRepository seatRepo;

    @Override
    public Optional<ConcertSeat> findForUpdate(Long performanceId, int seatNo) {
        return seatRepo.findByPerformanceIdAndSeatNoForUpdate(performanceId, seatNo);
    }

    @Override
    public ConcertSeat save(ConcertSeat seat) {
        return seatRepo.save(seat);
    }
}

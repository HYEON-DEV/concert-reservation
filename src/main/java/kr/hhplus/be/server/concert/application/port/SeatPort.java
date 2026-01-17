package kr.hhplus.be.server.concert.application.port;

import java.util.Optional;
import kr.hhplus.be.server.concert.domain.ConcertSeat;

public interface SeatPort {

    Optional<ConcertSeat> findForUpdate(Long performanceId, int seatNo);

    ConcertSeat save(ConcertSeat seat);
}

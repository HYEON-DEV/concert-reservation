package kr.hhplus.be.server.concert.infrastructure;

import java.util.List;
import kr.hhplus.be.server.concert.domain.ConcertSeat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertSeatJpaRepository extends JpaRepository<ConcertSeat, Long> {
//    List<ConcertSeat> findByPerformanceIdOrderBySeatNoAsc(Long performanceId);

    List<ConcertSeat> findAllByPerformanceIdOrderBySeatNoAsc(Long performanceId);
}

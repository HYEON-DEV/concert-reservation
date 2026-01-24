package kr.hhplus.be.server.concert.infrastructure;

import java.time.Instant;
import java.util.List;
import kr.hhplus.be.server.concert.domain.ConcertPerformance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertPerformanceJpaRepository extends JpaRepository<ConcertPerformance, Long> {

    List<ConcertPerformance> findAllByConcertIdOrderByStartAtAsc(Long concertId);
}

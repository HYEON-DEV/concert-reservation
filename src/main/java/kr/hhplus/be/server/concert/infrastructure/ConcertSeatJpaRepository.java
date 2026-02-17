package kr.hhplus.be.server.concert.infrastructure;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.concert.domain.ConcertSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConcertSeatJpaRepository extends JpaRepository<ConcertSeat, Long> {

    List<ConcertSeat> findAllByPerformanceIdOrderBySeatNoAsc(Long performanceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select s from ConcertSeat s
        where s.performanceId = :performanceId
          and s.seatNo = :seatNo
    """)
    Optional<ConcertSeat> findByPerformanceIdAndSeatNoForUpdate(
        @Param("performanceId") Long performanceId,
        @Param("seatNo") int seatNo
    );

    @Modifying
    @Query(value = """
        UPDATE concert_seat
        SET status = 'AVAILABLE',
            hold_user_id = NULL,
            hold_until = NULL
        WHERE status = 'HOLD'
          AND hold_until <= :now
    """, nativeQuery = true)
    int releaseExpiredHolds(@Param("now") java.time.Instant now);
}

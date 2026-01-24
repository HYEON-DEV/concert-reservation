package kr.hhplus.be.server.concert.application;

import java.time.Instant;
import java.util.List;
import kr.hhplus.be.server.concert.infrastructure.ConcertPerformanceJpaRepository;
import kr.hhplus.be.server.concert.infrastructure.ConcertSeatJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ConcertService {

    private final ConcertPerformanceJpaRepository performanceRepo;
    private final ConcertSeatJpaRepository seatRepo;

    @Transactional(readOnly = true)
    public List<PerformanceDto> getPerformances(Long concertId) {
        return performanceRepo.findAllByConcertIdOrderByStartAtAsc(concertId).stream()
            .map(p -> new PerformanceDto(p.id(), p.startAt()))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<SeatDto> getSeats(Long performanceId) {
        return seatRepo.findAllByPerformanceIdOrderBySeatNoAsc(performanceId).stream()
            .map(s -> new SeatDto(s.seatNo(), s.status().name()))
            .toList();
    }

    public record PerformanceDto(Long performanceId, Instant startAt) {}
    public record SeatDto(int seatNo, String status) {}
}

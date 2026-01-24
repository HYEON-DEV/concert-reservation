package kr.hhplus.be.server.concert.interfaces;

import java.util.List;
import kr.hhplus.be.server.concert.application.ConcertService;
import kr.hhplus.be.server.concert.application.ConcertService.PerformanceDto;
import kr.hhplus.be.server.concert.application.ConcertService.SeatDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/concerts")
@RequiredArgsConstructor
public class ConcertController {
    private final ConcertService concertService;

    @GetMapping("/{concertId}/performances")
    public ResponseEntity<PerformancesResponse> getPerformances(
        @PathVariable Long concertId
    ) {
        List<ConcertService.PerformanceDto> performances = concertService.getPerformances(concertId);
        return ResponseEntity.ok(new PerformancesResponse(concertId, performances));
    }

    @GetMapping("/performances/{performanceId}/seats")
    public ResponseEntity<SeatsResponse> getSeats(
        @PathVariable Long performanceId
    ) {
        List<ConcertService.SeatDto> seats = concertService.getSeats(performanceId);
        return ResponseEntity.ok(new SeatsResponse(performanceId, seats));
    }

    public record PerformancesResponse(Long concertId, List<ConcertService.PerformanceDto> performances) {}
    public record SeatsResponse(Long performanceId, List<SeatDto> seats) {}
}

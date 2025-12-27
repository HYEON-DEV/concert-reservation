package kr.hhplus.be.server.point.interfaces;

import kr.hhplus.be.server.point.application.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/points")
public class PointController {
    private final PointService pointService;

    // HTTP 요청/응답 DTO
    public record ChargeRequest(String userId, long amount){}
    public record PointResponse(String userId, long balance){}

    @GetMapping("/{userId}")
    public ResponseEntity<PointResponse> get(@PathVariable String userId) {
        long balance = pointService.getBalance(userId);
        return ResponseEntity.ok(new PointResponse(userId, balance));
    }

    @PostMapping("/charge")
    public ResponseEntity<PointResponse> charge(@RequestBody ChargeRequest req) {
        long balance = pointService.charge(req.userId(), req.amount());
        return ResponseEntity.ok(new PointResponse(req.userId(), balance));
    }
}

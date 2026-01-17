package kr.hhplus.be.server.concert.interfaces;

import kr.hhplus.be.server.concert.application.usecase.PayReservationUseCase;
import kr.hhplus.be.server.concert.application.usecase.ReserveSeatUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/concerts")
public class ReservationController {

    private final ReserveSeatUseCase reserveSeatUseCase;
    private final PayReservationUseCase payReservationUseCase;

    public record HoldRequest(String userId, Long performanceId, int seatNo) {}
    public record PayRequest(String userId, Long performanceId, int seatNo, long amount){}

    @PostMapping("/seats/hold")
    public ResponseEntity<ReserveSeatUseCase.Result> hold(@RequestBody HoldRequest req) {
        var result = reserveSeatUseCase.reserve(
            new ReserveSeatUseCase.Command(req.userId, req.performanceId(), req.seatNo()));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/seats/pay")
    public ResponseEntity<PayReservationUseCase.Result> pay(@RequestBody PayRequest req) {
        var result = payReservationUseCase.pay(
            new PayReservationUseCase.Command(
                req.userId, req.performanceId(), req.seatNo(), req.amount()));
        return ResponseEntity.ok(result);
    }
}

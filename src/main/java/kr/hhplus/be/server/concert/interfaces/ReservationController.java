package kr.hhplus.be.server.concert.interfaces;

import kr.hhplus.be.server.concert.application.usecase.PayReservationUseCase;
import kr.hhplus.be.server.concert.application.usecase.ReserveSeatUseCase;
import kr.hhplus.be.server.queue.application.QueueTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/concerts")
public class ReservationController {

    private final ReserveSeatUseCase reserveSeatUseCase;
    private final PayReservationUseCase payReservationUseCase;
    private final QueueTokenService queueTokenService;

    public record HoldRequest(String userId, Long performanceId, int seatNo) {}
    public record PayRequest(String userId, Long performanceId, int seatNo, long amount){}

    @PostMapping("/seats/hold")
    public ResponseEntity<ReserveSeatUseCase.Result> hold(
        @RequestHeader("X-QUEUE-TOKEN") String token,
        @RequestBody HoldRequest req
    ) {
        queueTokenService.validate(token);

        var result = reserveSeatUseCase.reserve(
            new ReserveSeatUseCase.Command(req.userId(), req.performanceId(), req.seatNo()));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/seats/pay")
    public ResponseEntity<PayReservationUseCase.Result> pay(
        @RequestHeader("X-QUEUE-TOKEN") String token,
        @RequestBody PayRequest req
    ) {
        queueTokenService.validate(token);

        var result = payReservationUseCase.pay(
            new PayReservationUseCase.Command(
                req.userId, req.performanceId(), req.seatNo(), req.amount()));

        queueTokenService.expire(token);

        return ResponseEntity.ok(result);
    }
}

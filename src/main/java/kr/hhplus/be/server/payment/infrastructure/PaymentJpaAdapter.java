package kr.hhplus.be.server.payment.infrastructure;

import java.time.Instant;
import kr.hhplus.be.server.concert.application.port.PaymentPort;
import kr.hhplus.be.server.payment.domain.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentJpaAdapter implements PaymentPort {

    private final PaymentJpaRepository paymentRepo;

    @Override
    public void record(
        String userId, Long performanceId, int seatNo, long amount, Instant paidAt) {
        paymentRepo.save(new Payment(userId, performanceId, seatNo, amount, paidAt));
    }

}

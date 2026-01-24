package kr.hhplus.be.server.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment",
    indexes = {
        @Index(name = "idx_payment_user_id", columnList = "user_id"),
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "performance_id", nullable = false)
    private Long performanceId;

    @Column(name = "seat_no", nullable = false)
    private int seatNo;

    @Column(name = "amount", nullable = false)
    private long amount;

    @Column(name = "paid_at", nullable = false, columnDefinition = "DATETIME(3)")
    private Instant paidAt;

//    @Column(name = "status", nullable = false, length = 20)
//    private String status;

    public Payment(
        String userId, Long performanceId, int seatNo, long amount, Instant paidAt) {
        this.userId = Objects.requireNonNull(userId);
        this.performanceId = Objects.requireNonNull(performanceId);
        this.seatNo = seatNo;
        this.amount = amount;
        this.paidAt = Objects.requireNonNull(paidAt);
    }
}

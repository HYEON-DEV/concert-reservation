package kr.hhplus.be.server.concert.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "concert_seat",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_performance_seat_no",
            columnNames = {"performance_id", "seat_no"})
    },
    indexes = {
        @Index(name = "idx_performance_id", columnList = "performance_id")
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConcertSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "performance_id", nullable = false)
    private Long performanceId;

    @Column(name = "seat_no", nullable = false)
    private int seatNo; // 1~50

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;

    @Column(name = "hold_user_id", length = 64)
    private String holdUserId;

    @Column(name = "hold_until", columnDefinition = "DATETIME(3)")
    private Instant holdUntil;

    public ConcertSeat(Long performanceId, int seatNo) {
        this.performanceId = Objects.requireNonNull(performanceId);
        if (seatNo < 1 || seatNo > 50)
            throw new IllegalArgumentException("seatNo must be 1~50");
        this.seatNo = seatNo;
        this.status = SeatStatus.AVAILABLE;
    }

    public Long id() { return id; }
    public Long performanceId() { return performanceId; }
    public int seatNo() { return seatNo; }
    public SeatStatus status() { return status; }
    public String holdUserId() { return holdUserId; }
    public Instant holdUntil() { return holdUntil; }

    public boolean isHoldExpired(Instant now) {
        return status == SeatStatus.HOLD && holdUntil != null && now.isAfter(holdUntil);
    }

    public void releaseIfExpired(Instant now) {
        if (isHoldExpired(now)) release();
    }

//    public boolean canBeHeldBy(String userId, Instant now) {
//        if (status == SeatStatus.AVAILABLE) return true;
//
//        if (status == SeatStatus.HOLD) {
//            // 만료됐으면 누구나 점유 가능
//            if (isHoldExpired(now)) return true;
//            // 만료 전이면 hold한 사람만
//            return Objects.equals(holdUserId, userId);
//        }
//        return false; // RESERVED면 불가
//    }

    public void hold(String userId, Instant until) {
        if (status == SeatStatus.RESERVED) throw new IllegalStateException("seat already reserved");
        this.status = SeatStatus.HOLD;
        this.holdUserId = Objects.requireNonNull(userId);
        this.holdUntil = Objects.requireNonNull(until);
    }

    public void reserve(String userId) {
        if (status == SeatStatus.RESERVED) throw new IllegalStateException("seat already reserved");
        if (status != SeatStatus.HOLD) throw new IllegalStateException("seat is not held");
        if (!Objects.equals(this.holdUserId, userId)) throw new IllegalStateException("not seat holder");
        this.status = SeatStatus.RESERVED;
        this.holdUserId = null;
        this.holdUntil = null;
    }

    public void release() {
        this.status = SeatStatus.AVAILABLE;
        this.holdUserId = null;
        this.holdUntil = null;
    }
}

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

    public ConcertSeat(Long performanceId, int seatNo, SeatStatus status) {
        this.performanceId = Objects.requireNonNull(performanceId);
        if (seatNo < 1 || seatNo > 50)
            throw new IllegalArgumentException("seatNo must be 1~50");
        this.seatNo = seatNo;
        this.status = Objects.requireNonNull(status);
    }

    public Long id() { return id; }
    public Long performanceId() { return performanceId; }
    public int seatNo() { return seatNo; }
    public SeatStatus status() { return status; }

    public void markHold() { this.status = SeatStatus.HOLD; }
    public void markReserved() { this.status = SeatStatus.RESERVED; }
    public void markAvailable() { this.status = SeatStatus.AVAILABLE; }
}

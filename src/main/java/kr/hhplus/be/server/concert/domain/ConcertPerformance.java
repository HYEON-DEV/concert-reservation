package kr.hhplus.be.server.concert.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table( name = "concert_performance",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_concert_id_start_at", columnNames = {"concert_id", "start_at"})
        },
        indexes = {
            @Index(name = "idx_concert_id_start_at", columnList = "concert_id, start_at")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConcertPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "concert_id", nullable = false)
    private Long concertId;

    @Column(name = "start_at", nullable = false, columnDefinition = "DATETIME(3)")
    private Instant startAt;

    public ConcertPerformance(Long concertId, Instant startAt) {
        this.concertId = Objects.requireNonNull(concertId);
        this.startAt = Objects.requireNonNull(startAt);
    }

    public Long id() { return id; }
    public Long concertId() { return concertId; }
    public Instant startAt() { return startAt; }
}

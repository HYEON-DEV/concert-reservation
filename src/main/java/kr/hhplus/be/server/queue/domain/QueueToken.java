package kr.hhplus.be.server.queue.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "queue_token",
    indexes = {
        @Index(name = "idx_token", columnList = "token", unique = true),
        @Index(name = "idx_user_id", columnList = "user_id")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QueueToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 64)
    private String token;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "issued_at", nullable = false, columnDefinition = "DATETIME(3)")
    private Instant issuedAt;

    @Column(name = "expired_at", nullable = false, columnDefinition = "DATETIME(3)")
    private Instant expiredAt;

    @Column(name = "active", nullable = false)
    private boolean active;

    public QueueToken(String userId, Instant issuedAt, Instant expiredAt) {
        this.token = UUID.randomUUID().toString();
        this.userId = userId;
        this.issuedAt = issuedAt;
        this.expiredAt = expiredAt;
        this.active = true;
    }

    public String token() { return token; }

    public String userId() { return userId; }

    public boolean isExpired(Instant now) {
        return !active || now.isAfter(expiredAt);
    }

    public void expire() {
        this.active = false;
    }
}

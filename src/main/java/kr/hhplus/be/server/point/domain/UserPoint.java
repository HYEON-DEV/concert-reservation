package kr.hhplus.be.server.point.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.Objects;

@Entity
@Table(name = "user_point")
public class UserPoint {

    @Id
    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    // 도메인 비즈니스 값이라 null 허용X
    // null 체크 필요 -> long(primitive)
    @Column(name = "balance", nullable = false)
    private long balance;

    // 비즈니스 데이터 X
    // jpa가 동시성 제어를 위해 내부적으로 관리하는 메타 정보
    @Version
    private Long version;

    protected UserPoint() {}

    public UserPoint(String userId, long balance) {
        this.userId = Objects.requireNonNull(userId); // NullPointerException 발생
        this.balance = balance;
    }

    public String userId() { return userId; }
    public long balance() { return balance; }

    public void charge(long amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive.");
        this.balance += amount;
    }

}

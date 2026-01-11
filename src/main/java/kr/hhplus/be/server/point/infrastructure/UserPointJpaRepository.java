package kr.hhplus.be.server.point.infrastructure;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import kr.hhplus.be.server.point.domain.UserPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserPointJpaRepository extends JpaRepository<UserPoint, String> {

    @Modifying
    @Query(value = """
        INSERT INTO user_point (user_id, balance, version)
        VALUES (:userId, :amount, 0)
        ON DUPLICATE KEY UPDATE
            balance = balance + VALUES(balance),
            version = version + 1
    """, nativeQuery = true)
    void upsertCharge(@Param("userId") String userId, @Param("amount") long amount);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from UserPoint p where p.userId = :userId")
    Optional<UserPoint> findByUserIdForUpdate(@Param("userId") String userId);
}

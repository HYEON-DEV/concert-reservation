package kr.hhplus.be.server.point.infrastructure;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import kr.hhplus.be.server.point.domain.UserPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserPointJpaRepository extends JpaRepository<UserPoint, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from UserPoint p where p.userId = :userId")
    Optional<UserPoint> findByUserIdForUpdate(@Param("userId") String userId);
}

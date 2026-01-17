package kr.hhplus.be.server.queue.infrastructure;

import java.time.Instant;
import java.util.Optional;
import kr.hhplus.be.server.queue.domain.QueueToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QueueTokenJpaRepository extends JpaRepository<QueueToken, Long> {

    Optional<QueueToken> findByToken(String token);

    @Query("""
      select count(q)
      from QueueToken q
      where q.active = true
          and q.expiredAt > :now
    """)
    long countActiveTokens(Instant now);
}

package kr.hhplus.be.server.point.application;

import kr.hhplus.be.server.point.domain.UserPoint;
import kr.hhplus.be.server.point.infrastructure.UserPointJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PointService {

    private final UserPointJpaRepository repo;

    @Transactional(readOnly = true)
    public long getBalance(String userId) {
        return repo.findById(userId)
                .map(UserPoint::balance)
                .orElse(0L);
    }

    @Transactional
    public long charge(String userId, long amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
        repo.upsertCharge(userId, amount);
        return repo.findById(userId).orElseThrow().balance();
    }

    @Transactional
    public void use(String userId, long amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive");

        int updated = repo.decreaseIfEnough(userId, amount);

        if (updated == 0) throw new IllegalStateException("not enough point");
    }
}


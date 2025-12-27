package kr.hhplus.be.server.point.application;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import kr.hhplus.be.server.point.domain.UserPoint;
import kr.hhplus.be.server.point.infrastructure.UserPointJpaRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;


class PointServiceTest {

    @Mock
    UserPointJpaRepository repo;

    @InjectMocks
    PointService pointService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("포인트 조회: 없으면 0 반환")
    void getBalance_returnsZero_whenNotExists() {
        when(repo.findById("u1")).thenReturn(Optional.empty());

        long balance = pointService.getBalance("u1");

        assertEquals(0L, balance);
        verify(repo).findById("u1");
    }

    @Test
    @DisplayName("포인트 충전: 기존 유저는 잔액 증가")
    void charge_existingUser_increaseBalance() {
        UserPoint p = new UserPoint("u1", 1000L);
        when(repo.findByUserIdForUpdate("u1")).thenReturn(Optional.of(p));

        long balance = pointService.charge("u1", 500L);

        assertEquals(1500L, balance);
        verify(repo).findByUserIdForUpdate("u1");
        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("포인트 충전: 신규 유저면 생성 후 충전")
    void charge_newUser_createAndCharge() {
        when(repo.findByUserIdForUpdate("u1")).thenReturn(Optional.empty());
        when(repo.save(any(UserPoint.class))).thenAnswer(inv -> inv.getArgument(0));

        long balance = pointService.charge("u1", 300L);

        assertEquals(300L, balance);
        verify(repo).findByUserIdForUpdate("u1");
        verify(repo).save(any(UserPoint.class));
    }

    @Test
    @DisplayName("포인트 충전 실패: amount가 0이하이면 예외")
    void charge_fail_whenAmountNonPositive() {
        UserPoint p = new UserPoint("u1", 1000L);
        when(repo.findByUserIdForUpdate("u1")).thenReturn(Optional.of(p));

        assertThrows(IllegalArgumentException.class,
            () -> pointService.charge("u1", 0L));
    }
}

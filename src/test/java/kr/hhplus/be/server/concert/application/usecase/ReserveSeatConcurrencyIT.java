package kr.hhplus.be.server.concert.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import kr.hhplus.be.server.concert.application.port.SeatHoldPort;
import kr.hhplus.be.server.concert.domain.ConcertSeat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.concert.infrastructure.ConcertSeatJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest
class ReserveSeatConcurrencyIT {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.jpa.show-sql", () -> "false");
    }

    @Autowired
    ReserveSeatUseCase reserveSeatUseCase;

    @Autowired
    ConcertSeatJpaRepository seatRepository;

    @MockBean
    SeatHoldPort seatHoldPort;

    @BeforeEach
    void setUp() {
        seatRepository.deleteAll();
        seatRepository.save(new ConcertSeat(1L, 1));

        when(seatHoldPort.hold(any(), any(), any(), any())).thenReturn(true);
    }

    @Test
    @DisplayName("동일 좌석 동시 요청 시 정확히 1건만 HOLD 성공한다")
    void reserveSameSeatConcurrently_onlyOneSucceeds() throws Exception {
        int threadCount = 20;

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        List<Boolean> success = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final String userId = "user-" + i;
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    reserveSeatUseCase.reserve(new ReserveSeatUseCase.Command(userId, 1L, 1));
                    synchronized (success) {
                        success.add(true);
                    }
                } catch (Exception ignored) {
                    synchronized (success) {
                        success.add(false);
                    }
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        done.await();
        pool.shutdown();

        long successCount = success.stream().filter(Boolean::booleanValue).count();
        assertEquals(1, successCount);

        ConcertSeat seat = seatRepository.findByPerformanceIdAndSeatNoForUpdate(1L, 1).orElseThrow();
        assertEquals(SeatStatus.HOLD, seat.status());
        assertTrue(seat.holdUserId().startsWith("user-"));
    }
}

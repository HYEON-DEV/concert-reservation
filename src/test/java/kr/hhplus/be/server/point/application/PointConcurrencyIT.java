package kr.hhplus.be.server.point.application;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import kr.hhplus.be.server.point.domain.UserPoint;
import kr.hhplus.be.server.point.infrastructure.UserPointJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class PointConcurrencyIT {

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
        // 테스트에서 테이블 자동 생성
        registry.add("spring.jpa.hibernate.ddl-auto", ()->"update");
        registry.add("spring.jpa.show-sql", ()->"true");
        registry.add("logging.level.org.hibernate.tool.schema", () -> "DEBUG");
    }

    @Autowired PointService pointService;
    @Autowired UserPointJpaRepository repo;

    @BeforeEach
    void clean() {
        repo.deleteAll();
    }

    @Test
    @DisplayName("이미 존재하는 유저에 대해, 여러 스레드가 동시 충전해도 합산값이 그대로 남아야 한다.")
    void charge_concurrently_existing_user_should_not_lose_updates() throws Exception {
        String userId = "u1";
        repo.save(new UserPoint(userId, 0L));

        int threadCount = 10;
        long amount = 100L;

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        for (int i=0; i<threadCount; i++) {
            pool.submit(() -> {
                ready.countDown(); // 시작, ready감소
                try {
                    start.await(); // 대기
                    pointService.charge(userId, amount);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    done.countDown(); // 각 스레드 끝나면, done감소
                }
            });
        }

        ready.await(); // 모든 스레드 다 준비될 때까지 대기
        start.countDown(); // 한 번에 동시 시작
        done.await(); // 전부 끝날때까지 대기
        pool.shutdown(); // 스레드풀 종료

        UserPoint saved = repo.findById(userId).orElseThrow();
        assertEquals(threadCount * amount, saved.balance());
    }

    @Test
    @DisplayName("포인트 동시 충전: 신규 유저면 생성 후 충전")
    void charge_concurrently_new_user_should_not_fail_and_sum_correct() throws Exception {
        String userId = "new_user";
        int threadCount = 10;
        long amount = 100L;

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        // 모든 작업이 준비될 때까지 메인 스레드 대기
        CountDownLatch ready = new CountDownLatch(threadCount);
        // 준비 끝난 스레드들이 start.await() 에서 대기하다가
        // 메인 스레드가 start.countDown() 을 호출하면 전원이 동시 시작
        CountDownLatch start = new CountDownLatch(1);

        try {
            List<CompletableFuture<Void>> futures = IntStream.range(0, threadCount)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    ready.countDown(); // 준비됨 -> ready latch 카운트 감소
                    try {
                        start.await(); // 동시에 시작 대기
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                    pointService.charge(userId, amount);
                }, pool))
                .toList();

            // 메인 스레드가 모든 작업이 ready.countDown 호출할 때까지 대기
            ready.await();
            // start.await에 막혀있던 모든 작업이 거의 동시에 시작
            start.countDown();

            // allOf: 모든 future가 끝날 때까지 기다리는 future를 만든다
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            UserPoint saved = repo.findById(userId).orElseThrow();
            assertEquals(threadCount * amount, saved.balance());
        } finally {
            pool.shutdown();
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        }
    }

}

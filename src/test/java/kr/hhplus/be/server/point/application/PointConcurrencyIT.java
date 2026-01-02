package kr.hhplus.be.server.point.application;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import kr.hhplus.be.server.point.domain.UserPoint;
import kr.hhplus.be.server.point.infrastructure.UserPointJpaRepository;
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
        registry.add("spring.jpa.hibernate.ddl-auto", ()->"create-drop");
        registry.add("spring.jpa.show-sql", ()->"true");
    }

    @Autowired PointService pointService;
    @Autowired UserPointJpaRepository repo;

    @Test
    @DisplayName("이미 존재하는 유저에 대해, 여러 스레드가 동시 충전해도 합산값이 그대로 남아야 한다.")
    void charge_concurrently_existing_user_should_not_lose_updates() throws Exception {
//        System.out.println("DOCKER_HOST env = " + System.getenv("DOCKER_HOST"));
//        System.out.println("DOCKER_HOST prop = " + System.getProperty("DOCKER_HOST"));

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
}

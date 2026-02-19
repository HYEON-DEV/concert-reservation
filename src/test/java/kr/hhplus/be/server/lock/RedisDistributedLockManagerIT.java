package kr.hhplus.be.server.lock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import kr.hhplus.be.server.point.infrastructure.UserPointJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest
class RedisDistributedLockManagerIT {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.4"))
        .withExposedPorts(6379);

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    RedisDistributedLockManager lockManager;

    @Autowired
    UserPointJpaRepository userPointJpaRepository;

    @BeforeEach
    void clean() {
        userPointJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("동일 키 분산락은 동시에 하나의 요청만 획득한다")
    void onlyOneThreadCanAcquireSameLockKey() throws Exception {
        String key = "lock:user_point:u1";
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch entered = new CountDownLatch(1);

        Future<Boolean> first = pool.submit(() -> {
            lockManager.executeWithLock(
                key,
                Duration.ofSeconds(2),
                Duration.ofMillis(300),
                Duration.ofMillis(20),
                () -> {
                    entered.countDown();
                    sleep(400);
                }
            );
            return true;
        });

        Future<Boolean> second = pool.submit(() -> {
            entered.await();
            try {
                lockManager.executeWithLock(
                    key,
                    Duration.ofSeconds(2),
                    Duration.ofMillis(120),
                    Duration.ofMillis(20),
                    () -> {
                    }
                );
                return true;
            } catch (DistributedLockAcquisitionException e) {
                return false;
            }
        });

        assertTrue(first.get());
        assertFalse(second.get());
        pool.shutdownNow();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}

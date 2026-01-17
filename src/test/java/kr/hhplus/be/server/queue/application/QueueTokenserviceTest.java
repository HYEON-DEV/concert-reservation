package kr.hhplus.be.server.queue.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import kr.hhplus.be.server.queue.domain.QueueStatus;
import kr.hhplus.be.server.queue.infrastructure.RedisQueueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class QueueTokenServiceTest {

    RedisQueueRepository repo;
    Clock clock;
    QueueTokenService service;

    @BeforeEach
    void setUp() {
        repo = Mockito.mock(RedisQueueRepository.class);
        clock = Clock.fixed(Instant.parse("2026-01-17T00:00:00Z"), ZoneOffset.UTC);
        service = new QueueTokenService(repo, clock);
    }

    @Test
    void issue_should_reuse_existing_token_if_present() {
        // given
        String userId = "u1";
        String token = "t-123";

        given(repo.findTokenByUserId(userId)).willReturn(Optional.of(token));
        given(repo.findUserIdByToken(token)).willReturn(Optional.of(userId));
        given(repo.getRank(token)).willReturn(1L);
        given(repo.getSize()).willReturn(10L);

        // when
        var issued = service.issue(userId);

        // then
        assertEquals(token, issued.token());
        assertEquals(userId, issued.userId());
        assertEquals(QueueStatus.ACTIVE, issued.status());

        then(repo).should(never()).saveToken(anyString(), anyString(), any());
        then(repo).should(never()).enqueue(anyString(), any());
    }

    @Test
    void validate_should_throw_when_waiting() {
        // given
        String token = "t-999";
        String userId = "u9";

        given(repo.findUserIdByToken(token)).willReturn(Optional.of(userId));
        given(repo.getRank(token)).willReturn(101L); // ACTIVE_LIMIT=100 초과
        given(repo.getSize()).willReturn(200L);

        // when & then
        assertThrows(IllegalStateException.class, () -> service.validate(token));
    }

    @Test
    void validate_should_pass_when_active() {
        // given
        String token = "t-100";
        String userId = "u1";

        given(repo.findUserIdByToken(token)).willReturn(Optional.of(userId));
        given(repo.getRank(token)).willReturn(50L); // ACTIVE
        given(repo.getSize()).willReturn(200L);

        // when & then
        assertDoesNotThrow(() -> service.validate(token));
    }
}

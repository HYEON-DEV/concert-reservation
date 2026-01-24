package kr.hhplus.be.server.queue.domain;

import java.time.Instant;

public record QueueToken(
    String token,
    String userId,
    QueueStatus status,
    long position,
    long totalWaiting,  // 대기열 전체 인원
    Instant issuedAt,
    Instant expiresAt
) {}

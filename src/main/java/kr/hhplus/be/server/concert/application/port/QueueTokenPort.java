package kr.hhplus.be.server.concert.application.port;

public interface QueueTokenPort {
    void validate(String token, String userId);
    void expire(String token);
}

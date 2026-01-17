package kr.hhplus.be.server.queue.interfaces;

import kr.hhplus.be.server.queue.application.QueueTokenService;
import kr.hhplus.be.server.queue.domain.QueueToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/queue")
public class QueueTokenController {

    private final QueueTokenService service;

    public record IssueRequest(String userId){}
    public record TokenResponse(String token, String userId) {}

    public ResponseEntity<TokenResponse> issue(@RequestBody IssueRequest req) {
        QueueToken token = service.issue(req.userId());
        return ResponseEntity.ok(
            new TokenResponse(token.token(), token.userId())
        );
    }
}

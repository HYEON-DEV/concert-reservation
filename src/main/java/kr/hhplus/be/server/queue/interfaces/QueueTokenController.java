package kr.hhplus.be.server.queue.interfaces;

import kr.hhplus.be.server.queue.application.QueueTokenService;
import kr.hhplus.be.server.queue.domain.QueueToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/queue")
public class QueueTokenController {

    private final QueueTokenService queueTokenService;

    public record IssueRequest(String userId){}

    @PostMapping("/token")
    public ResponseEntity<QueueToken> issue(@RequestBody IssueRequest req) {
        return ResponseEntity.ok(queueTokenService.issue(req.userId()));
    }

    @GetMapping
    public ResponseEntity<QueueToken> me(@RequestHeader("X-QUEUE-TOKEN") String token) {
        return ResponseEntity.ok(queueTokenService.me(token));
    }
}

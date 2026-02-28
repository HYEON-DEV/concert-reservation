package kr.hhplus.be.server.ranking.interfaces;

import java.util.List;
import kr.hhplus.be.server.ranking.application.RankingQueryService;
import kr.hhplus.be.server.ranking.application.RankingQueryService.RankingItem;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rankings")
public class RankingController {

    private final RankingQueryService rankingQueryService;

    @GetMapping("/concerts/daily")
    public ResponseEntity<RankingResponse> daily(
        @RequestParam(name = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(new RankingResponse(rankingQueryService.topDaily(limit)));
    }

    @GetMapping("/concerts/weekly")
    public ResponseEntity<RankingResponse> weekly(
        @RequestParam(name = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(new RankingResponse(rankingQueryService.topWeekly(limit)));
    }

    public record RankingResponse(List<RankingItem> rankings) {
    }
}

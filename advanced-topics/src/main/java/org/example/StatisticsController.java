package org.example;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
public class StatisticsController {

    public record CountRequest(String mode, List<String> filePaths) {}
    public record CountResponse(Map<Character, Long> counts, long totalLetters) {}

    private final LruResultsCache cache = new LruResultsCache(1000);

    private final TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(200, Duration.ofSeconds(1));

    @PostMapping("/count")
    public ResponseEntity<CountResponse> handleCountRequest(@RequestBody CountRequest request) {
        if (request.filePaths() == null || request.filePaths().isEmpty() || request.mode() == null) {
            throw new IllegalArgumentException("Mode and filePaths are required.");
        }

        if (!rateLimiter.tryAcquire(request.mode())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        List<Path> paths = request.filePaths().stream().map(Path::of).toList();

        Bench.Output result = switch (request.mode()) {
            case "sync" -> Strategies.synchronizedBlock(paths);
            case "chm" -> Strategies.concurrentHashMap(paths);
            case "pool" -> Strategies.fixedPool(paths, cache);
            default -> throw new IllegalArgumentException("Unsupported mode: " + request.mode());
        };

        var response = new CountResponse(result.counts(), result.totalLetters());
        return ResponseEntity.ok(response);
    }
}
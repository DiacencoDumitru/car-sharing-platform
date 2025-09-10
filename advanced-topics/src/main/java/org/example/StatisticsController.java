package org.example;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
public class StatisticsController {

    public record CountRequest(String mode, List<String> filePaths) {}
    public record CountResponse(Map<Character, Long> counts, long totalLetters) {}

    private final LruResultsCache cache;

    public StatisticsController(LruResultsCache cache) {
        this.cache = cache;
    }

    @PostMapping("/count")
    public CountResponse handleCountRequest(@RequestBody CountRequest request) {
        if (request.filePaths() == null || request.filePaths().isEmpty() || request.mode() == null) {
            throw new IllegalArgumentException("Mode and filePaths are required.");
        }

        List<Path> paths = request.filePaths().stream().map(Path::of).toList();

        Bench.Output result = switch (request.mode()) {
            case "sync" -> Strategies.synchronizedBlock(paths);
            case "chm" -> Strategies.concurrentHashMap(paths);
            case "pool" -> Strategies.fixedPool(paths, cache); // uses the configured, bigger cache
            default -> throw new IllegalArgumentException("Unsupported mode: " + request.mode());
        };

        return new CountResponse(result.counts(), result.totalLetters());
    }
}
package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Stream;

public class LoadGeneratorApplication {

    public record CountRequest(String mode, List<String> filePaths) {}

    public static void main(String[] args) throws Exception {
        final int targetRps = Integer.parseInt(System.getProperty("rps", "100"));
        final String durationProp = System.getProperty("duration", "30s");
        final String url = System.getProperty("url", "http://localhost:8080/count");
        final String sourceDir = System.getProperty("source", ".");
        final int workerThreads = Integer.parseInt(System.getProperty("workers", "16"));
        final String mode = System.getProperty("mode", "pool");

        final int nodeCount = Integer.parseInt(System.getProperty("node.count", "1"));
        final int nodeIndex = Integer.parseInt(System.getProperty("node.index", "0"));
        if (nodeIndex < 0 || nodeIndex >= nodeCount) {
            throw new IllegalArgumentException("node.index must be in [0, node.count).");
        }
        final int myRps = splitRps(targetRps, nodeCount, nodeIndex);

        final Duration duration = parseDurationFlexible(durationProp);
        System.out.printf("""
                Starting load test with config:
                  Total RPS=%d | node.count=%d | node.index=%d -> this node RPS=%d
                  Duration=%s
                  URL=%s
                  Source=%s
                  Workers=%d
                  Mode=%s

                """, targetRps, nodeCount, nodeIndex, myRps, duration, url, sourceDir, workerThreads, mode);

        final List<String> sampleFilePaths;
        try (Stream<Path> stream = Files.walk(Path.of(sourceDir), 5)) {
            sampleFilePaths = stream.filter(Files::isRegularFile)
                    .limit(5)
                    .map(p -> p.toAbsolutePath().toString())
                    .toList();
        }
        if (sampleFilePaths.isEmpty()) {
            System.err.println("No files found in source directory. Cannot create request payload.");
            return;
        }
        final ObjectMapper mapper = new ObjectMapper();
        final byte[] payload = mapper.writeValueAsBytes(new CountRequest(mode, sampleFilePaths));

        final HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        final ExecutorService sendPool = Executors.newFixedThreadPool(workerThreads);

        final StatisticsAggregator stats = new StatisticsAggregator();

        final long intervalNanos = (long) (1_000_000_000.0 / myRps);
        final long totalRequests = Math.max(0L, Math.round((duration.toNanos() * 1.0) / intervalNanos));
        System.out.printf("This node will send %,d requests (interval ~ %,d ns).\n", totalRequests, intervalNanos);

        final CountDownLatch done = new CountDownLatch((int) Math.min(Integer.MAX_VALUE, totalRequests));
        final long start = System.nanoTime();

        Thread pacer = new Thread(() -> {
            for (long i = 0; i < totalRequests; i++) {
                long scheduledTime = start + i * intervalNanos;
                long waitNanos = scheduledTime - System.nanoTime();
                if (waitNanos > 0) {
                    LockSupport.parkNanos(waitNanos);
                }
                sendPool.execute(() -> sendRequest(client, url, payload, stats, done));
            }
        }, "pacer");
        pacer.setDaemon(true);
        pacer.start();

        pacer.join();

        boolean finished = done.await(Math.max(10, (int) duration.plusSeconds(30).toSeconds()), TimeUnit.SECONDS);

        sendPool.shutdownNow();

        if (!finished) {
            System.out.println("Not all responses arrived before timeout; remaining: " + done.getCount());
        }

        stats.printReport();
    }

    private static void sendRequest(HttpClient client, String url, byte[] payload, StatisticsAggregator stats, CountDownLatch done) {
        final long startNs = System.nanoTime();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofByteArray(payload))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .whenComplete((resp, err) -> {
                    long latencyMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
                    try {
                        if (err == null && resp != null && resp.statusCode() == 200) {
                            stats.recordSuccess(latencyMs);
                        } else {
                            stats.recordFailure();
                        }
                    } finally {
                        done.countDown();
                    }
                });
    }

    private static int splitRps(int totalRps, int nodes, int idx) {
        int base = totalRps / nodes;
        int extra = totalRps % nodes;
        return base + (idx < extra ? 1 : 0);
    }

    private static Duration parseDurationFlexible(String raw) {
        String v = Objects.requireNonNullElse(raw, "").trim();
        if (v.isEmpty()) return Duration.ofSeconds(30);
        try {
            if (v.startsWith("PT") || v.startsWith("pt")) return Duration.parse(v.toUpperCase());
            char last = Character.toUpperCase(v.charAt(v.length() - 1));
            String num = (Character.isDigit(last) ? v : v.substring(0, v.length() - 1)).trim();
            long n = Long.parseLong(num);
            return switch (last) {
                case 'S' -> Duration.ofSeconds(n);
                case 'M' -> Duration.ofMinutes(n);
                case 'H' -> Duration.ofHours(n);
                default -> Duration.ofSeconds(Long.parseLong(v));
            };
        } catch (Exception e) {
            throw new IllegalArgumentException("Bad duration '" + raw + "'. Try 30s, 2m, 1h, or ISO-8601 PT30S.", e);
        }
    }
}

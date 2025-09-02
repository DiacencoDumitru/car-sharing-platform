package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LoadGeneratorApplication {

    public record CountRequest(String mode, List<String> filePaths) {}

    private static final Set<String> TEXT_EXTENSIONS =
            Set.of("txt", "md", "java", "xml", "yml", "yaml", "properties", "csv", "json", "html");

    public static void main(String[] args) throws IOException, InterruptedException {
        int rps = Integer.parseInt(System.getProperty("rps", "100"));
        Duration duration = Duration.parse("PT" + System.getProperty("duration", "30s"));
        String url = System.getProperty("url", "http://localhost:8080/count");
        String sourceDir = System.getProperty("source", ".");
        int workerThreads = Integer.parseInt(System.getProperty("workers", "16"));

        System.out.printf("Starting load test with config:%n  RPS=%d%n  Duration=%s%n  URL=%s%n  Source=%s%n%n",
                rps, duration, url, sourceDir);

        final StatisticsAggregator stats = new StatisticsAggregator();
        final ObjectMapper objectMapper = new ObjectMapper();
        final HttpClient client = HttpClient.newBuilder()
                .executor(Executors.newCachedThreadPool())
                .build();

        List<String> sampleFilePaths;
        try (var stream = Files.walk(Path.of(sourceDir), 10)) {
            sampleFilePaths = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> TEXT_EXTENSIONS.contains(ext(p)))
                    .map(Path::toString)
                    .limit(5) // Send 5 files per request
                    .collect(Collectors.toList());
        }
        if (sampleFilePaths.isEmpty()) {
            System.err.println("No text files found in source directory. Cannot create request payload.");
            return;
        }
        byte[] payload = objectMapper.writeValueAsBytes(new CountRequest("pool", sampleFilePaths));

        ThreadPoolExecutor workerPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(workerThreads);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        System.out.println("Starting load generation...");
        long intervalNanos = 1_000_000_000 / rps;

        scheduler.scheduleAtFixedRate(() -> {
            if (workerPool.getQueue().size() < workerThreads * 2) {
                workerPool.submit(() -> sendRequest(client, url, payload, stats));
            }
        }, 0, intervalNanos, TimeUnit.NANOSECONDS);


        Thread.sleep(duration.toMillis());

        System.out.println("Test duration finished. Shutting down...");
        scheduler.shutdownNow();
        workerPool.shutdown();
        workerPool.awaitTermination(10, TimeUnit.SECONDS);

        stats.printReport();
    }

    private static void sendRequest(HttpClient client, String url, byte[] payload, StatisticsAggregator stats) {
        long startTime = System.nanoTime();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofByteArray(payload))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .whenComplete((response, error) -> {
                    long latencyMillis = (System.nanoTime() - startTime) / 1_000_000;
                    if (error == null && response.statusCode() == 200) {
                        stats.recordSuccess(latencyMillis);
                    } else {
                        stats.recordFailure();
                    }
                });
    }

    private static String ext(Path p) {
        String n = String.valueOf(p.getFileName());
        int i = n.lastIndexOf('.');
        return i < 0 ? "" : n.substring(i + 1).toLowerCase(Locale.ROOT);
    }
}
package org.example;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

public class LetterStatisticsApplication {
    public static void main(String[] args) throws Exception {
        var parsed = parseArgs(args);
        if (parsed.root == null) {
            System.err.println("Usage: --root <folder> [--modes unsafe,sync,chm,striped,mapreduce,mapreduce-parallel,pool] [--ext txt,md,java]");
            System.exit(2);
        }

        Set<String> exts = FileWalker.parseExts(parsed.extsArg);
        List<Path> files = FileWalker.listFiles(parsed.root, exts);
        if (files.isEmpty()) {
            System.err.println("No files found under: " + parsed.root + " with exts=" + exts);
            System.exit(1);
        }
        System.out.printf("Found %,d files under %s (exts=%s)%n", files.size(), parsed.root, exts);

        List<String> modes = parsed.modes.isEmpty() ?
                List.of("unsafe", "sync", "chm", "striped", "mapreduce", "mapreduce-parallel", "pool") :
                parsed.modes;

        LruResultsCache cache = new LruResultsCache(Math.min(1000, files.size()));

        System.out.println("\n--- First Run (Populating Cache) ---");
        Map<String, Bench.Result> firstRunResults = runBenchmark(modes, files, cache);

        System.out.println("\n--- Second Run (Hitting Cache) ---");
        runBenchmark(modes, files, cache);

        System.out.println("\n" + cache.stats());

        var baseline = Stream.of("sync", "chm", "striped", "mapreduce", "mapreduce-parallel", "pool")
                .filter(firstRunResults::containsKey)
                .map(firstRunResults::get)
                .findFirst().orElse(null);

        if (baseline != null) {
            for (var e : firstRunResults.entrySet()) {
                if (e.getValue().suspectedIncorrect()) continue;
                if (!e.getValue().counts().equals(baseline.counts())) {
                    System.out.printf("❌ Mismatch vs baseline in mode %s%n", e.getKey());
                }
            }
        }
    }

    private static Map<String, Bench.Result> runBenchmark(List<String> modes, List<Path> files, LruResultsCache cache) {
        Map<String, Bench.Result> results = new LinkedHashMap<>();
        for (String mode : modes) {
            Bench.Result r = switch (mode) {
                case "unsafe" -> Bench.time("unsafe (shared HashMap, no sync)", () -> Strategies.unsafeSharedHashMap(files));
                case "sync" -> Bench.time("sync (synchronized block)", () -> Strategies.synchronizedBlock(files));
                case "chm" -> Bench.time("chm (ConcurrentHashMap<LongAdder>)", () -> Strategies.concurrentHashMap(files));
                case "striped" -> Bench.time("striped (26 locks + int[26])", () -> Strategies.stripedLocks(files));
                case "mapreduce" -> Bench.time("map/reduce (no sharing, sequential merge)", () -> Strategies.mapReduce(files, cache));
                case "mapreduce-parallel" -> Bench.time("map/reduce (parallel merge)", () -> Strategies.mapReduceParallelMerge(files, cache));
                case "pool" -> Bench.time("pool (fixed size = cores, map/reduce)", () -> Strategies.fixedPool(files, cache));
                default -> throw new IllegalArgumentException("Unknown mode: " + mode);
            };
            results.put(mode, r);
            System.out.printf("[%s] %s | %s | top: %s%s%n",
                    now(),
                    r.name(),
                    r.elapsed(),
                    Util.formatTop(r.counts(), 8),
                    r.suspectedIncorrect() ? "  (⚠ might be incorrect)" : ""
            );
        }
        return results;
    }

    private static String now() {
        return DateTimeFormatter.ISO_LOCAL_TIME.format(java.time.LocalTime.now().withNano(0));
    }

    record Parsed(Path root, String extsArg, List<String> modes) {}

    private static Parsed parseArgs(String[] args) {
        Path root = null;
        String exts = null;
        List<String> modes = List.of();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--root" -> root = Path.of(args[++i]);
                case "--ext" -> exts = args[++i];
                case "--modes" -> modes = Arrays.asList(args[++i].split(","));
            }
        }
        return new Parsed(root, exts, modes);
    }
}
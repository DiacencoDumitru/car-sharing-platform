package org.example;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

final class Strategies {

    // 1) UNSAFE: shared HashMap without synchronization (INCORRECT)
    static Bench.Output unsafeSharedHashMap(List<Path> files) {
        Map<Character, Long> shared = new HashMap<>();
        List<Thread> threads = new ArrayList<>();

        for (Path f : files) {
            Thread t = new Thread(() -> {
                try {
                    LetterTally tly = new LetterTally();
                    Util.countFileIntoTally(f, tly);
                    int[] arr = tly.snapshot();
                    for (int i = 0; i < 26; i++) {
                        int v = arr[i];
                        if (v == 0) continue;
                        char c = (char)('a' + i);
                        // non-atomic read-modify-write on an UNSAFE map
                        shared.compute(c, (k, cur) -> (cur == null ? 0L : cur) + v);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, "unsafe-" + f.getFileName());
            t.start();
            threads.add(t);
        }
        joinAll(threads);

        long total = shared.values().stream().mapToLong(Long::longValue).sum();
        return new Bench.Output(shared, total, true); // likely wrong
    }

    // 2) synchronized block around a plain HashMap (CORRECT but coarse-grained)
    static Bench.Output synchronizedBlock(List<Path> files) {
        Map<Character, Long> shared = new HashMap<>();
        List<Thread> threads = new ArrayList<>();

        for (Path f : files) {
            Thread t = new Thread(() -> {
                try {
                    LetterTally tly = new LetterTally();
                    Util.countFileIntoTally(f, tly);
                    int[] arr = tly.snapshot();
                    synchronized (shared) { // coarse-grained lock (all letters)
                        for (int i = 0; i < 26; i++) {
                            int v = arr[i];
                            if (v == 0) continue;
                            char c = (char)('a' + i);
                            shared.merge(c, (long) v, Long::sum);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, "sync-" + f.getFileName());
            t.start();
            threads.add(t);
        }
        joinAll(threads);

        long total = shared.values().stream().mapToLong(Long::longValue).sum();
        return new Bench.Output(shared, total, false);
    }

    // 3) ConcurrentHashMap<Character, LongAdder> (CORRECT, high concurrency)
    static Bench.Output concurrentHashMap(List<Path> files) {
        ConcurrentHashMap<Character, LongAdder> shared = new ConcurrentHashMap<>();
        List<Thread> threads = new ArrayList<>();

        for (Path f : files) {
            Thread t = new Thread(() -> {
                try {
                    LetterTally tly = new LetterTally();
                    Util.countFileIntoTally(f, tly);
                    int[] arr = tly.snapshot();
                    for (int i = 0; i < 26; i++) {
                        int v = arr[i];
                        if (v == 0) continue;
                        char c = (char)('a' + i);
                        shared.computeIfAbsent(c, k -> new LongAdder()).add(v);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, "chm-" + f.getFileName());
            t.start();
            threads.add(t);
        }
        joinAll(threads);

        Map<Character, Long> out = LetterTally.toMap(shared);
        long total = out.values().stream().mapToLong(Long::longValue).sum();
        return new Bench.Output(out, total, false);
    }

    // 4) Custom striped locks per letter (CORRECT, specialized)
    static Bench.Output stripedLocks(List<Path> files) {
        StripedCounter counter = new StripedCounter();
        List<Thread> threads = new ArrayList<>();

        for (Path f : files) {
            Thread t = new Thread(() -> {
                try (var br = java.nio.file.Files.newBufferedReader(f, java.nio.charset.StandardCharsets.UTF_8)) {
                    int ch;
                    while ((ch = br.read()) != -1) counter.addChar((char) ch);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, "striped-" + f.getFileName());
            t.start();
            threads.add(t);
        }
        joinAll(threads);

        var out = counter.toMap();
        return new Bench.Output(out, counter.total(), false);
    }

    // 5) Map/Reduce: (use cache)
    static Bench.Output mapReduce(List<Path> files, LruResultsCache cache) {
        List<Thread> threads = new ArrayList<>();
        List<int[]> partials = new CopyOnWriteArrayList<>();

        for (Path f : files) {
            Thread t = new Thread(() -> {
                try {
                    partials.add(Util.countFileWithCache(f, cache));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, "mr-" + f.getFileName());
            t.start();
            threads.add(t);
        }
        joinAll(threads);

        int[] merged = new int[26];
        for (int[] p : partials) {
            for (int i = 0; i < 26; i++) merged[i] += p[i];
        }
        var m = LetterTally.toMap(merged);
        long total = m.values().stream().mapToLong(Long::longValue).sum();
        return new Bench.Output(m, total, false);
    }

    // 6) Map/Reduce with PARALLEL merge: (use cache)
    static Bench.Output mapReduceParallelMerge(List<Path> files, LruResultsCache cache) {
        List<int[]> partials = new CopyOnWriteArrayList<>();
        List<Thread> threads = new ArrayList<>();

        for (Path f : files) {
            Thread t = new Thread(() -> {
                try {
                    partials.add(Util.countFileWithCache(f, cache));
                }
                catch (IOException e) { throw new RuntimeException(e); }
            }, "mrp-" + f.getFileName());
            t.start();
            threads.add(t);
        }
        joinAll(threads);

        int[] merged = partials.parallelStream().reduce(new int[26], (a, b) -> {
            for (int i = 0; i < 26; i++) a[i] += b[i];
            return a;
        }, (a, b) -> {
            for (int i = 0; i < 26; i++) a[i] += b[i];
            return a;
        });

        var m = LetterTally.toMap(merged);
        long total = m.values().stream().mapToLong(Long::longValue).sum();
        return new Bench.Output(m, total, false);
    }

    // 7) Thread pool (use cache)
    static Bench.Output fixedPool(List<Path> files, LruResultsCache cache) {
        int n = Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(n);
        try {
            List<Future<int[]>> futures = new ArrayList<>();
            for (Path f : files) {
                futures.add(pool.submit(() -> Util.countFileWithCache(f, cache)));
            }
            int[] merged = new int[26];
            for (Future<int[]> fut : futures) {
                int[] p = fut.get();
                for (int i = 0; i < 26; i++) merged[i] += p[i];
            }
            var m = LetterTally.toMap(merged);
            long total = m.values().stream().mapToLong(Long::longValue).sum();
            return new Bench.Output(m, total, false);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } finally {
            pool.shutdownNow();
        }
    }

    private static void joinAll(List<Thread> threads) {
        for (Thread t : threads) {
            try { t.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }
}

/*  // Required Java 21+
    static Bench.Output virtualThreads(List<Path> files) {
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<int[]>> futures = new ArrayList<>();
            for (Path f : files) {
                futures.add(pool.submit(() -> Util.countFileIntoArray(f)));
            }
            int[] merged = new int[26];
            for (Future<int[]> fut : futures) {
                int[] p = fut.get(); // .get() blocks until the future is complete
                for (int i = 0; i < 26; i++) merged[i] += p[i];
            }
            var m = LetterTally.toMap(merged);
            long total = m.values().stream().mapToLong(Long::longValue).sum();
            return new Bench.Output(m, total, false);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
*/
package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class Util {

    private Util() {}

    static void countFileIntoTally(Path file, LetterTally tally) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            int ch;
            while ((ch = br.read()) != -1) {
                tally.addChar((char) ch);
            }
        }
    }

    static int[] countFileIntoArray(Path file) throws IOException {
        LetterTally t = new LetterTally();
        countFileIntoTally(file, t);
        return t.snapshot();
    }

    static String formatTop(Map<Character, Long> counts, int n) {
        List<Map.Entry<Character, Long>> sorted = counts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(n)
                .toList();
        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        return sorted.stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", ")) + " | total=" + total;
    }

    public static int[] countFileWithCache(Path file, LruResultsCache cache) throws IOException {
        int[] cachedCounts = cache.get(file);
        if (cachedCounts != null) {
            return cachedCounts;
        }

        int[] computedCounts = countFileIntoArray(file);

        cache.put(file, computedCounts);

        return computedCounts;
    }
}

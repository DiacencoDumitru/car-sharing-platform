package org.example;

public record CacheStats(long hits, long misses, long evictions, int currentSize) {
    @Override
    public String toString() {
        long total = hits + misses;
        double hitRate = total == 0 ? 0 : (double) hits * 100 / total;
        return String.format(
            "Cache Stats: size=%,d, hits=%,d, misses=%,d, evictions=%,d, hitRate=%.2f%%",
            currentSize, hits, misses, evictions, hitRate
        );
    }
}
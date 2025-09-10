package org.example;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class LruResultsCache {
    private final int maxSize;
    private final Map<Path, int[]> map;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final LongAdder hits = new LongAdder();
    private final LongAdder misses = new LongAdder();
    private final LongAdder evictions = new LongAdder();

    public LruResultsCache(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("Max size must be positive");
        }
        this.maxSize = maxSize;
        this.map = new LinkedHashMap<>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Path, int[]> eldest) {
                boolean shouldEvict = size() > LruResultsCache.this.maxSize;
                if (shouldEvict) {
                    evictions.increment();
                }
                return shouldEvict;
            }
        };
    }

    public int[] get(Path key) {
        lock.readLock().lock();
        try {
            int[] result = map.get(key);
            if (result != null) {
                hits.increment();
            } else {
                misses.increment();
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void put(Path key, int[] counts) {
        lock.writeLock().lock();
        try {
            map.put(key, counts);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public CacheStats stats() {
        lock.readLock().lock();
        try {
            return new CacheStats(hits.sum(), misses.sum(), evictions.sum(), map.size());
        } finally {
            lock.readLock().unlock();
        }
    }
}
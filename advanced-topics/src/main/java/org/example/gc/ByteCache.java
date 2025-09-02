package org.example.gc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ByteCache {
    private final int maxEntries;
    private final Map<String, byte[]> store = new ConcurrentHashMap<>();
    private final AtomicLong approxBytes = new AtomicLong();

    public ByteCache(int maxEntries) {
        this.maxEntries = Math.max(1, maxEntries);
    }

    public int maxEntries() { return maxEntries; }

    public void put(String key, byte[] payload) {
        byte[] prev = store.put(key, payload);
        if (prev != null) approxBytes.addAndGet(-prev.length);
        approxBytes.addAndGet(payload.length);
    }

    public void clear() {
        store.clear();
        approxBytes.set(0);
    }

    public int size() {
        return store.size();
    }

    public long approxBytes() {
        return approxBytes.get();
    }
}

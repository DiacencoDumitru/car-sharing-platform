package org.example;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

final class StripedCounter {
    private final int[] counts = new int[26];
    private final ReentrantLock[] locks = new ReentrantLock[26];

    StripedCounter() {
        for (int i = 0; i < locks.length; i++) locks[i] = new ReentrantLock();
    }

    void addChar(char c) {
        int idx = LetterTally.indexOf(c);
        if (idx >= 0) {
            ReentrantLock L = locks[idx];
            L.lock();
            try {
                counts[idx]++;
            } finally {
                L.unlock();
            }
        }
    }

    Map<Character, Long> toMap() {
        return LetterTally.toMap(counts);
    }

    long total() {
        long t = 0;
        for (int v : counts) t += v;
        return t;
    }
}

package org.example;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

final class LetterTally {
    private final int[] a = new int[26];

    void addChar(char c) {
        int idx = indexOf(c);
        if (idx >= 0) a[idx]++;
    }

    void addArray(int[] arr) {
        for (int i = 0; i < 26; i++) a[i] += arr[i];
    }

    int[] snapshot() {
        return Arrays.copyOf(a, a.length);
    }

    long total() {
        long t = 0;
        for (int v : a) t += v;
        return t;
    }

    static int indexOf(char c) {
        if (c >= 'A' && c <= 'Z') c = (char)(c + 32);
        if (c >= 'a' && c <= 'z') return c - 'a';
        return -1;
    }

    static Map<Character, Long> toMap(int[] arr) {
        Map<Character, Long> m = new LinkedHashMap<>();
        for (int i = 0; i < 26; i++) {
            long v = arr[i];
            if (v > 0) m.put((char)('a' + i), v);
        }
        return m;
    }

    static Map<Character, Long> toMap(ConcurrentHashMap<Character, LongAdder> chm) {
        Map<Character, Long> m = new LinkedHashMap<>();
        chm.forEach((k, v) -> m.put(k, v.longValue()));
        return m;
    }
}

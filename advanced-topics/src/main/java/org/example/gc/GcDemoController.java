package org.example.gc;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.random.RandomGenerator;

@RestController
@RequestMapping("/gc")
public class GcDemoController {

    private final ByteCache byteCache;

    public GcDemoController(ByteCache byteCache) {
        this.byteCache = byteCache;
    }

    @PostMapping("/fill")
    public Map<String, Object> fill(
            @RequestParam(defaultValue = "200000") int entries,
            @RequestParam(name = "payloadKB", defaultValue = "4") int payloadKB) {

        entries = Math.min(entries, byteCache.maxEntries());
        int size = Math.max(1, payloadKB) * 1024;

        var rnd = RandomGenerator.getDefault();
        byte[] sample = new byte[size];

        for (int i = 0; i < entries; i++) {
            rnd.nextBytes(sample);
            byte[] payload = sample.clone();
            byteCache.put("entry-" + i, payload);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("filledEntries", entries);
        res.put("payloadBytesEach", size);
        res.put("cacheSize", byteCache.size());
        res.put("approxBytes", byteCache.approxBytes());
        return res;
    }

    @DeleteMapping("/clear")
    public Map<String, Object> clear() {
        byteCache.clear();
        return Map.of("cacheSize", byteCache.size(), "approxBytes", byteCache.approxBytes());
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return Map.of(
                "maxEntries", byteCache.maxEntries(),
                "cacheSize", byteCache.size(),
                "approxBytes", byteCache.approxBytes()
        );
    }
}

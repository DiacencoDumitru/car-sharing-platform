package org.example.config;

import org.example.LruResultsCache;
import org.example.gc.ByteCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public LruResultsCache resultsCache(
            @Value("${letters.cache.maxSize:500000}") int maxSize) {
        return new LruResultsCache(maxSize);
    }

    @Bean
    public ByteCache gcByteCache(
            @Value("${gc.byteCache.maxEntries:500000}") int maxEntries) {
        return new ByteCache(maxEntries);
    }
}

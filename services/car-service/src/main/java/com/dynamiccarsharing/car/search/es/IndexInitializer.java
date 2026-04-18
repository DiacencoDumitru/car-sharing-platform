package com.dynamiccarsharing.car.search.es;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class IndexInitializer {

    private final ElasticsearchOperations operations;

    @Bean
    ApplicationRunner createCarIndexIfMissing() {
        return args -> {
            int attempts = 0, max = 10;
            long backoffMs = 1500;

            while (attempts < max) {
                try {
                    var indexOps = operations.indexOps(CarDocument.class);
                    if (!indexOps.exists()) {
                        indexOps.create();
                        indexOps.putMapping(indexOps.createMapping());
                        log.info("Created Elasticsearch index and mapping for {}", CarDocument.class.getSimpleName());
                    } else {
                        log.info("Elasticsearch index for {} already exists", CarDocument.class.getSimpleName());
                    }
                    return;
                } catch (Exception e) {
                    attempts++;
                    log.warn("ES index init attempt {}/{} failed: {}. Retrying in {} ms...",
                            attempts, max, e.getMessage(), backoffMs);
                    try { Thread.sleep(backoffMs); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
            log.error("Failed to initialize Elasticsearch index after {} attempts. The service will still run; indexing/search may fail until ES is reachable.", max);
        };
    }
}
package com.dynamiccarsharing.gateway.filter;

import com.dynamiccarsharing.gateway.cache.RedisResponseCacheService;
import com.dynamiccarsharing.gateway.cache.ResponseCacheKeyFactory;
import com.dynamiccarsharing.gateway.config.ResponseCacheProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
@Slf4j
public class ResponseCacheInvalidationFilter implements GlobalFilter, Ordered {

    private final RedisResponseCacheService cacheService;
    private final ResponseCacheKeyFactory keyFactory;
    private final ResponseCacheProperties properties;

    public ResponseCacheInvalidationFilter(
            RedisResponseCacheService cacheService,
            ResponseCacheKeyFactory keyFactory,
            ResponseCacheProperties properties
    ) {
        this.cacheService = cacheService;
        this.keyFactory = keyFactory;
        this.properties = properties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!properties.isEnabled() || !isMutationMethod(exchange.getRequest().getMethod())) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();
        Set<String> groups = keyFactory.resolveInvalidationGroups(path);
        if (groups.isEmpty()) {
            return chain.filter(exchange);
        }

        return chain.filter(exchange)
                .then(Mono.defer(() -> {
                    HttpStatusCode status = exchange.getResponse().getStatusCode();
                    if (status == null || !status.is2xxSuccessful()) {
                        return Mono.empty();
                    }
                    return cacheService.evictByGroups(groups)
                            .onErrorResume(ex -> {
                                log.warn("Failed to invalidate response cache groups={}", groups, ex);
                                return Mono.empty();
                            });
                }));
    }

    @Override
    public int getOrder() {
        return -5;
    }

    private boolean isMutationMethod(HttpMethod method) {
        return method == HttpMethod.POST
                || method == HttpMethod.PUT
                || method == HttpMethod.PATCH
                || method == HttpMethod.DELETE;
    }
}

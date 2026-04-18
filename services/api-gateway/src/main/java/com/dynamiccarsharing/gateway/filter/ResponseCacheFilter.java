package com.dynamiccarsharing.gateway.filter;

import com.dynamiccarsharing.gateway.cache.CachedHttpResponse;
import com.dynamiccarsharing.gateway.cache.RedisResponseCacheService;
import com.dynamiccarsharing.gateway.cache.ResponseCacheKeyFactory;
import com.dynamiccarsharing.gateway.config.ResponseCacheProperties;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
@Slf4j
public class ResponseCacheFilter implements GlobalFilter, Ordered {

    private final RedisResponseCacheService cacheService;
    private final ResponseCacheKeyFactory keyFactory;
    private final ResponseCacheProperties properties;

    public ResponseCacheFilter(
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
        if (!properties.isEnabled()) {
            return chain.filter(exchange);
        }

        if (exchange.getRequest().getMethod() != HttpMethod.GET) {
            return chain.filter(exchange);
        }

        if (!keyFactory.isCacheable(exchange.getRequest())) {
            return chain.filter(exchange);
        }

        if (isCacheBypassRequested(exchange)) {
            return chain.filter(exchange);
        }

        String key = keyFactory.buildKey(exchange.getRequest());
        Set<String> groups = keyFactory.resolveGroupsForPath(exchange.getRequest().getURI().getPath());

        return cacheService.get(key)
                .flatMap(cached -> writeCachedResponse(exchange, cached))
                .switchIfEmpty(Mono.defer(() -> filterAndCacheResponse(exchange, chain, key, groups)));
    }

    @Override
    public int getOrder() {
        return -10;
    }

    private Mono<Void> filterAndCacheResponse(
            ServerWebExchange exchange,
            GatewayFilterChain chain,
            String key,
            Set<String> groups
    ) {
        ServerHttpResponse originalResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();

        ServerHttpResponseDecorator decorated = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (!(body instanceof Flux)) {
                    return super.writeWith(body);
                }
                return DataBufferUtils.join(Flux.from(body))
                        .flatMap(dataBuffer -> {
                            byte[] content = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(content);
                            DataBufferUtils.release(dataBuffer);

                            HttpStatusCode statusCode = getStatusCode();
                            if (!isResponseCacheable(statusCode, getHeaders(), content.length) || groups.isEmpty()) {
                                return super.writeWith(Mono.just(bufferFactory.wrap(content)));
                            }
                            int statusValue = Objects.requireNonNull(statusCode).value();
                            CachedHttpResponse cached = new CachedHttpResponse(
                                    statusValue,
                                    copyHeaders(getHeaders()),
                                    content
                            );
                            return cacheService.put(key, cached, groups)
                                    .onErrorResume(ex -> {
                                        log.warn("Failed to cache response key={}", key, ex);
                                        return Mono.empty();
                                    })
                                    .then(super.writeWith(Mono.just(bufferFactory.wrap(content))));
                        });
            }
        };

        return chain.filter(exchange.mutate().response(decorated).build());
    }

    private Mono<Void> writeCachedResponse(ServerWebExchange exchange, CachedHttpResponse cached) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatusCode.valueOf(cached.getStatusCode()));
        cached.getHeaders().forEach((name, values) -> response.getHeaders().put(name, values));
        DataBuffer buffer = response.bufferFactory().wrap(cached.getBody());
        return response.writeWith(Mono.just(buffer));
    }

    private boolean isCacheBypassRequested(ServerWebExchange exchange) {
        String cacheControl = exchange.getRequest().getHeaders().getFirst("Cache-Control");
        return cacheControl != null && cacheControl.toLowerCase().contains("no-cache");
    }

    private boolean isResponseCacheable(HttpStatusCode statusCode, org.springframework.http.HttpHeaders headers, int bodySize) {
        if (statusCode == null || !statusCode.is2xxSuccessful() || statusCode.value() != 200) {
            return false;
        }
        if (bodySize > properties.getMaxBodySizeBytes()) {
            return false;
        }
        String cacheControl = headers.getFirst("Cache-Control");
        return cacheControl == null
                || (!cacheControl.toLowerCase().contains("no-store")
                && !cacheControl.toLowerCase().contains("private"));
    }

    private Map<String, List<String>> copyHeaders(org.springframework.http.HttpHeaders headers) {
        Map<String, List<String>> copied = new LinkedHashMap<>();
        headers.forEach((name, values) -> copied.put(name, List.copyOf(values)));
        return copied;
    }
}

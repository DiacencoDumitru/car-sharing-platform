package com.dynamiccarsharing.gateway.cache;

import com.dynamiccarsharing.gateway.config.ResponseCacheProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Set;

@Service
@Slf4j
public class RedisResponseCacheService {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ResponseCacheKeyFactory keyFactory;
    private final ResponseCacheProperties properties;

    public RedisResponseCacheService(
            ReactiveRedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper,
            ResponseCacheKeyFactory keyFactory,
            ResponseCacheProperties properties
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.keyFactory = keyFactory;
        this.properties = properties;
    }

    public Mono<CachedHttpResponse> get(String key) {
        return redisTemplate.opsForValue().get(key)
                .flatMap(this::deserialize)
                .onErrorResume(ex -> {
                    log.warn("Failed to read response cache key={}", key, ex);
                    return Mono.empty();
                });
    }

    public Mono<Void> put(String key, CachedHttpResponse value, Set<String> groups) {
        return serialize(value)
                .flatMap(payload -> redisTemplate.opsForValue()
                        .set(key, payload, properties.getDefaultTtl())
                        .flatMap(saved -> saved ? Mono.just(saved) : Mono.empty()))
                .flatMapMany(saved -> Flux.fromIterable(groups))
                .flatMap(group -> registerGroupKey(group, key))
                .then()
                .onErrorResume(ex -> {
                    log.warn("Failed to write response cache key={}", key, ex);
                    return Mono.empty();
                });
    }

    public Mono<Void> evictByGroups(Set<String> groups) {
        return Flux.fromIterable(groups)
                .flatMap(this::evictGroup)
                .then()
                .onErrorResume(ex -> {
                    log.warn("Failed to evict response cache groups={}", groups, ex);
                    return Mono.empty();
                });
    }

    private Mono<Void> evictGroup(String group) {
        String groupKey = keyFactory.groupKey(group);
        return redisTemplate.opsForSet().members(groupKey)
                .collectList()
                .flatMap(keys -> {
                    Mono<Long> deleteResponses = keys.isEmpty()
                            ? Mono.just(0L)
                            : redisTemplate.delete(Flux.fromIterable(keys));
                    return deleteResponses.then(redisTemplate.delete(groupKey));
                })
                .then();
    }

    private Mono<Long> registerGroupKey(String group, String key) {
        String groupKey = keyFactory.groupKey(group);
        Duration ttl = properties.getDefaultTtl().multipliedBy(2);
        return redisTemplate.opsForSet().add(groupKey, key)
                .then(redisTemplate.expire(groupKey, ttl))
                .thenReturn(1L);
    }

    private Mono<CachedHttpResponse> deserialize(String payload) {
        try {
            return Mono.just(objectMapper.readValue(payload, CachedHttpResponse.class));
        } catch (JsonProcessingException ex) {
            return Mono.error(ex);
        }
    }

    private Mono<String> serialize(CachedHttpResponse response) {
        try {
            return Mono.just(objectMapper.writeValueAsString(response));
        } catch (JsonProcessingException ex) {
            return Mono.error(ex);
        }
    }
}

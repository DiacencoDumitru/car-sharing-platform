package com.dynamiccarsharing.booking.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

@Configuration
@ConditionalOnProperty(name = "application.redis.read-cache.enabled", havingValue = "true")
@EnableCaching
public class BookingRedisReadCacheConfig {

    @Value("${spring.cache.redis.key-prefix:}")
    private String keyPrefix;

    @Value("${spring.cache.redis.time-to-live:5m}")
    private Duration defaultTtl;

    @Bean
    @Primary
    public CacheManager bookingReadCacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        var valueSerializer = new GenericJackson2JsonRedisSerializer(om);

        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                .disableCachingNullValues()
                .entryTtl(defaultTtl)
                .computePrefixWith(cacheName ->
                        (keyPrefix == null || keyPrefix.isBlank() ? "" : keyPrefix) + cacheName + "::"
                );

        Map<String, RedisCacheConfiguration> perCache = Map.of(
                "bookingById", base.entryTtl(Duration.ofMinutes(10)),
                "bookingPage", base.entryTtl(Duration.ofMinutes(2)),
                "bookingsByRenterId", base.entryTtl(Duration.ofMinutes(3)),
                "bookingSearch", base.entryTtl(Duration.ofMinutes(2))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(base)
                .withInitialCacheConfigurations(perCache)
                .build();
    }
}

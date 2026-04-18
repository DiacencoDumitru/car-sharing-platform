package com.dynamiccarsharing.car.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RedisCacheConfigTest {

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    @InjectMocks
    private RedisCacheConfig redisCacheConfig;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(redisCacheConfig, "keyPrefix", "testPrefix:");
        ReflectionTestUtils.setField(redisCacheConfig, "defaultTtl", Duration.ofMinutes(1));
    }

    @Test
    @DisplayName("cacheManager bean creates RedisCacheManager with correct defaults and specific caches")
    void cacheManager_createsManagerWithDefaults() {
        CacheManager cacheManager = redisCacheConfig.cacheManager(redisConnectionFactory);
        assertInstanceOf(RedisCacheManager.class, cacheManager);

        Map<String, RedisCacheConfiguration> initialCacheConfigs = (Map<String, RedisCacheConfiguration>)
                ReflectionTestUtils.getField(cacheManager, "initialCacheConfiguration");
        
        assertNotNull(initialCacheConfigs);
        assertTrue(initialCacheConfigs.containsKey("carById"));
        assertTrue(initialCacheConfigs.containsKey("carReviewsByCarId"));
        assertTrue(initialCacheConfigs.containsKey("locationById"));
        assertTrue(initialCacheConfigs.containsKey("allLocations"));

        assertEquals(Duration.ofMinutes(15), initialCacheConfigs.get("carById").getTtl());
        assertEquals(Duration.ofMinutes(3), initialCacheConfigs.get("carReviewsByCarId").getTtl());
        assertEquals(Duration.ofHours(1), initialCacheConfigs.get("locationById").getTtl());
        assertEquals(Duration.ofHours(1), initialCacheConfigs.get("allLocations").getTtl());

        String prefix = initialCacheConfigs.get("carById").getKeyPrefixFor("carById");
        assertEquals("testPrefix:carById::", prefix);
    }

    @Test
    @DisplayName("cacheManager handles blank prefix correctly")
    void cacheManager_handlesBlankPrefix() {
         ReflectionTestUtils.setField(redisCacheConfig, "keyPrefix", " ");
         CacheManager cacheManager = redisCacheConfig.cacheManager(redisConnectionFactory);

         Map<String, RedisCacheConfiguration> initialCacheConfigs = (Map<String, RedisCacheConfiguration>)
                ReflectionTestUtils.getField(cacheManager, "initialCacheConfiguration");

         String prefix = initialCacheConfigs.get("carById").getKeyPrefixFor("carById");
         assertEquals("carById::", prefix);
    }

     @Test
    @DisplayName("cacheManager handles null prefix correctly")
    void cacheManager_handlesNullPrefix() {
         ReflectionTestUtils.setField(redisCacheConfig, "keyPrefix", null);
         CacheManager cacheManager = redisCacheConfig.cacheManager(redisConnectionFactory);

         Map<String, RedisCacheConfiguration> initialCacheConfigs = (Map<String, RedisCacheConfiguration>)
                ReflectionTestUtils.getField(cacheManager, "initialCacheConfiguration");
         
         String prefix = initialCacheConfigs.get("carById").getKeyPrefixFor("carById");
         assertEquals("carById::", prefix);
    }
}
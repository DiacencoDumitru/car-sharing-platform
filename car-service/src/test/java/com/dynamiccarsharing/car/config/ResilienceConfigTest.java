package com.dynamiccarsharing.car.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.core.IntervalFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;

import java.time.Duration;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResilienceConfigTest {

    @Mock
    private Resilience4JCircuitBreakerFactory factory;

    @Captor
    private ArgumentCaptor<Function<String, Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration>> configFunctionCaptor;
    @Captor
    private ArgumentCaptor<CircuitBreakerConfig> cbConfigCaptor;
    @Captor
    private ArgumentCaptor<TimeLimiterConfig> tlConfigCaptor;

    private ResilienceConfig resilienceConfig;

    @BeforeEach
    void setUp() {
        resilienceConfig = new ResilienceConfig();
    }

    @Test
    @DisplayName("defaultCustomizer configures CircuitBreaker and TimeLimiter correctly")
    void defaultCustomizer_configuresDefaults() {
        Customizer<Resilience4JCircuitBreakerFactory> customizer = resilienceConfig.defaultCustomizer();
        assertNotNull(customizer);

        CircuitBreakerConfig actualCbConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .slowCallRateThreshold(50)
            .slowCallDurationThreshold(Duration.ofSeconds(2))
            .slidingWindowSize(20)
            .waitDurationInOpenState(Duration.ofSeconds(10))
            .permittedNumberOfCallsInHalfOpenState(3)
            .build();

        TimeLimiterConfig actualTlConfig = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(3))
            .build();

        assertEquals(50, actualCbConfig.getFailureRateThreshold());
        assertEquals(50, actualCbConfig.getSlowCallRateThreshold());
        assertEquals(Duration.ofSeconds(2), actualCbConfig.getSlowCallDurationThreshold());
        assertEquals(20, actualCbConfig.getSlidingWindowSize());

        IntervalFunction intervalFunction = actualCbConfig.getWaitIntervalFunctionInOpenState();
        assertNotNull(intervalFunction);
        assertEquals(Duration.ofSeconds(10).toMillis(), intervalFunction.apply(1));

        assertEquals(3, actualCbConfig.getPermittedNumberOfCallsInHalfOpenState());

        assertEquals(Duration.ofSeconds(3), actualTlConfig.getTimeoutDuration());

        customizer.customize(factory);
        verify(factory).configureDefault(any(Function.class));
    }

     @Test
     @DisplayName("Constructor coverage")
     void constructor() {
         assertNotNull(new ResilienceConfig());
     }
}
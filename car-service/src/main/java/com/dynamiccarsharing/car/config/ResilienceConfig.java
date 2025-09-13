package com.dynamiccarsharing.car.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id ->
                new Resilience4JConfigBuilder(id)
                        .circuitBreakerConfig(
                                CircuitBreakerConfig.custom()
                                        .failureRateThreshold(50)
                                        .slowCallRateThreshold(50)
                                        .slowCallDurationThreshold(Duration.ofSeconds(2))
                                        .slidingWindowSize(20)
                                        .waitDurationInOpenState(Duration.ofSeconds(10))
                                        .permittedNumberOfCallsInHalfOpenState(3)
                                        .build()
                        )
                        .timeLimiterConfig(
                                TimeLimiterConfig.custom()
                                        .timeoutDuration(Duration.ofSeconds(3))
                                        .build()
                        )
                        .build()
        );
    }
}
package com.dynamiccarsharing.booking.service;

import com.dynamiccarsharing.booking.BookingApplication;
import com.dynamiccarsharing.booking.dto.BookingCreateRequestDto;
import com.dynamiccarsharing.booking.service.interfaces.BookingService;
import com.dynamiccarsharing.contracts.dto.CarDto;
import com.dynamiccarsharing.contracts.dto.UserDto;
import com.dynamiccarsharing.contracts.enums.CarStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.web.reactive.function.client.WebClient.Builder;

@SpringBootTest(
        classes = {BookingApplication.class, BookingRedisGuardIntegrationTest.MockWebClientConfig.class}
)
@ActiveProfiles({"integration", "jpa"})
@Testcontainers(disabledWithoutDocker = true)
class BookingRedisGuardIntegrationTest {

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7.2-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("application.redis.booking-guard.enabled", () -> "true");
        registry.add("application.redis.booking-guard.wait-timeout-millis", () -> "2500");
    }

    @Autowired
    private BookingService bookingService;

    @TestConfiguration
    static class MockWebClientConfig {
        @Bean
        @Primary
        Builder webClientBuilder() {
            WebClient userWebClient = mockWebClientReturningUser();
            WebClient carWebClient = mockWebClientReturningAvailableCar();
            Builder mockBuilder = mock(Builder.class);
            Builder userBuilder = mock(Builder.class);
            Builder carBuilder = mock(Builder.class);
            when(mockBuilder.baseUrl("lb://user-service")).thenReturn(userBuilder);
            when(mockBuilder.baseUrl("lb://car-service")).thenReturn(carBuilder);
            when(userBuilder.build()).thenReturn(userWebClient);
            when(carBuilder.build()).thenReturn(carWebClient);
            return mockBuilder;
        }

        private static WebClient mockWebClientReturningUser() {
            WebClient wc = mock(WebClient.class);
            var uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
            WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
            when(wc.get()).thenReturn(uriSpec);
            when(uriSpec.uri(anyString())).thenReturn(uriSpec);
            when(uriSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(UserDto.class)).thenReturn(Mono.just(new UserDto()));
            return wc;
        }

        private static WebClient mockWebClientReturningAvailableCar() {
            WebClient wc = mock(WebClient.class);
            var uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
            WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
            CarDto car = new CarDto();
            car.setStatus(CarStatus.AVAILABLE);
            when(wc.get()).thenReturn(uriSpec);
            when(uriSpec.uri(anyString())).thenReturn(uriSpec);
            when(uriSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(any(Class.class))).thenReturn(Mono.just(car));
            return wc;
        }
    }

    @Test
    @DisplayName("Redis booking guard allows only one concurrent overlapping booking")
    void redisGuard_blocksConcurrentOverlappingBookings() throws Exception {
        int workers = 10;
        CountDownLatch ready = new CountDownLatch(workers);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(workers);

        LocalDateTime baseStart = LocalDateTime.now().plusDays(1);
        LocalDateTime baseEnd = baseStart.plusHours(8);
        Long carId = 777L;
        Long pickupId = 17L;

        List<Callable<Boolean>> jobs = new ArrayList<>();
        for (int i = 0; i < workers; i++) {
            long renterId = 1000L + i;
            jobs.add(() -> {
                BookingCreateRequestDto dto = new BookingCreateRequestDto();
                dto.setRenterId(renterId);
                dto.setCarId(carId);
                dto.setStartTime(baseStart);
                dto.setEndTime(baseEnd);
                dto.setPickupLocationId(pickupId);

                ready.countDown();
                if (!start.await(3, TimeUnit.SECONDS)) {
                    return false;
                }

                try {
                    bookingService.save(dto);
                    return true;
                } catch (Exception ex) {
                    return false;
                }
            });
        }

        List<Future<Boolean>> futures = new ArrayList<>();
        for (Callable<Boolean> job : jobs) {
            futures.add(pool.submit(job));
        }

        assertThat(ready.await(3, TimeUnit.SECONDS)).isTrue();
        start.countDown();

        int success = 0;
        for (Future<Boolean> future : futures) {
            if (Boolean.TRUE.equals(future.get(5, TimeUnit.SECONDS))) {
                success++;
            }
        }
        pool.shutdownNow();

        assertThat(success).isEqualTo(1);
    }
}

package com.dynamiccarsharing.booking.cache;

import com.dynamiccarsharing.booking.BookingApplication;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = BookingApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"integration", "jpa"})
@Testcontainers(disabledWithoutDocker = true)
class BookingReadCacheIntegrationTest {

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7.2-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisAndCache(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("application.redis.read-cache.enabled", () -> "true");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @SpyBean
    private BookingRepository bookingRepository;

    @Test
    @DisplayName("GET booking by id: repository findById once when read-cache enabled")
    void getBookingById_secondHttpCallDoesNotHitRepositoryAgain() {
        Booking saved = bookingRepository.save(Booking.builder()
                .renterId(1L)
                .carId(2L)
                .pickupLocationId(3L)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .status(TransactionStatus.PENDING)
                .build());

        String url = "http://localhost:" + port + "/api/v1/bookings/" + saved.getId();

        assertThat(restTemplate.getForEntity(url, Object.class).getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(restTemplate.getForEntity(url, Object.class).getStatusCode().is2xxSuccessful()).isTrue();

        verify(bookingRepository, times(1)).findById(saved.getId());
    }
}

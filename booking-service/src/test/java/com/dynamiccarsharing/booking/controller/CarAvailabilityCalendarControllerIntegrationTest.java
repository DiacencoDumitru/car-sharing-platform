package com.dynamiccarsharing.booking.controller;

import com.dynamiccarsharing.booking.BookingApplication;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.booking.service.CarAvailabilityServiceImpl;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = {BookingApplication.class, CarAvailabilityCalendarControllerIntegrationTest.MockWebClientConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles({"integration", "jpa"})
class CarAvailabilityCalendarControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BookingRepository bookingRepository;

    private LocalDateTime windowStart;
    private LocalDateTime windowEnd;

    @BeforeEach
    void setUp() {
        bookingRepository.findAll().forEach(b -> bookingRepository.deleteById(b.getId()));
        windowStart = LocalDateTime.now().plusDays(7).withHour(10).withMinute(0).withSecond(0).withNano(0);
        windowEnd = windowStart.plusDays(3);
    }

    @Test
    @DisplayName("GET /api/v1/bookings/availability/calendar returns all available days when slot is free")
    void calendar_whenFree_returnsAllAvailable() {
        ResponseEntity<Map> response = restTemplate.getForEntity(url(200L), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("carId")).isEqualTo(200);
        List<Map<String, Object>> days = (List<Map<String, Object>>) response.getBody().get("days");
        assertThat(days).hasSize(4);
        assertThat(days).allSatisfy(day -> {
            assertThat(day.get("available")).isEqualTo(true);
            assertThat(day.get("reason")).isNull();
        });
    }

    @Test
    @DisplayName("GET /api/v1/bookings/availability/calendar returns all unavailable days when car is not available")
    void calendar_whenCarNotAvailable_returnsAllUnavailable() {
        ResponseEntity<Map> response = restTemplate.getForEntity(url(999L), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        List<Map<String, Object>> days = (List<Map<String, Object>>) response.getBody().get("days");
        assertThat(days).hasSize(4);
        assertThat(days).allSatisfy(day -> {
            assertThat(day.get("available")).isEqualTo(false);
            assertThat(day.get("reason")).isEqualTo(CarAvailabilityServiceImpl.REASON_CAR_NOT_AVAILABLE);
        });
    }

    @Test
    @DisplayName("GET /api/v1/bookings/availability/calendar marks overlapped days as SLOT_TAKEN")
    void calendar_whenOverlappingBooking_marksTakenDay() {
        bookingRepository.save(Booking.builder()
                .renterId(1L)
                .carId(300L)
                .pickupLocationId(1L)
                .startTime(windowStart.plusDays(1))
                .endTime(windowStart.plusDays(2))
                .status(TransactionStatus.PENDING)
                .build());

        ResponseEntity<Map> response = restTemplate.getForEntity(url(300L), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        List<Map<String, Object>> days = (List<Map<String, Object>>) response.getBody().get("days");
        assertThat(days).hasSize(4);
        assertThat(days.get(0).get("available")).isEqualTo(true);
        assertThat(days.get(1).get("available")).isEqualTo(false);
        assertThat(days.get(1).get("reason")).isEqualTo(CarAvailabilityServiceImpl.REASON_SLOT_TAKEN);
        assertThat(days.get(2).get("available")).isEqualTo(false);
        assertThat(days.get(2).get("reason")).isEqualTo(CarAvailabilityServiceImpl.REASON_SLOT_TAKEN);
        assertThat(days.get(3).get("available")).isEqualTo(true);
    }

    @Test
    @DisplayName("GET /api/v1/bookings/availability/calendar returns 400 when endTime is not after startTime")
    void calendar_invalidRange_returnsBadRequest() {
        String q = String.format(
                "?carId=400&startTime=%s&endTime=%s",
                windowEnd,
                windowStart
        );

        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/v1/bookings/availability/calendar" + q,
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private String url(long carId) {
        return "http://localhost:" + port + "/api/v1/bookings/availability/calendar?carId=" + carId
                + "&startTime=" + windowStart
                + "&endTime=" + windowEnd;
    }

    @TestConfiguration
    static class MockWebClientConfig {
        @Bean
        @Primary
        WebClient.Builder webClientBuilder() {
            return WebClient.builder().exchangeFunction(selectiveExchangeFunction());
        }

        private static ClientResponse jsonResponse(org.springframework.http.HttpStatus status, String json) {
            return ClientResponse.create(status)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(json)
                    .build();
        }

        private static ExchangeFunction selectiveExchangeFunction() {
            return request -> {
                String path = request.url().getPath();
                if (path.contains("/cars/999")) {
                    return Mono.just(jsonResponse(HttpStatus.OK, "{\"id\":999,\"status\":\"RENTED\",\"price\":120.00}"));
                }
                if (path.contains("/cars/")) {
                    return Mono.just(jsonResponse(HttpStatus.OK, "{\"id\":1,\"status\":\"AVAILABLE\",\"price\":120.00}"));
                }
                return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
            };
        }
    }
}

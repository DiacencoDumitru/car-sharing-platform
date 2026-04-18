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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = {BookingApplication.class, CarAvailabilityControllerIntegrationTest.MockWebClientConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles({"integration", "jpa"})
class CarAvailabilityControllerIntegrationTest {

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
        windowStart = LocalDateTime.now().plusDays(5).withHour(10).withMinute(0).withSecond(0).withNano(0);
        windowEnd = windowStart.plusHours(2);
    }

    @Test
    @DisplayName("GET /api/v1/bookings/availability returns available true when car is listed and slot is free")
    void availability_whenFree_returnsTrue() {
        ResponseEntity<Map> response = restTemplate.getForEntity(url(200L), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("available")).isEqualTo(true);
        assertThat(response.getBody().get("reason")).isNull();
    }

    @Test
    @DisplayName("GET /api/v1/bookings/availability returns CAR_NOT_AVAILABLE when car status is not AVAILABLE")
    void availability_whenCarNotListed_returnsCarNotAvailable() {
        ResponseEntity<Map> response = restTemplate.getForEntity(url(999L), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("available")).isEqualTo(false);
        assertThat(response.getBody().get("reason")).isEqualTo(CarAvailabilityServiceImpl.REASON_CAR_NOT_AVAILABLE);
    }

    @Test
    @DisplayName("GET /api/v1/bookings/availability returns SLOT_TAKEN when PENDING booking overlaps")
    void availability_whenOverlappingPending_returnsSlotTaken() {
        bookingRepository.save(Booking.builder()
                .renterId(1L)
                .carId(300L)
                .pickupLocationId(1L)
                .startTime(windowStart)
                .endTime(windowEnd)
                .status(TransactionStatus.PENDING)
                .build());

        ResponseEntity<Map> response = restTemplate.getForEntity(url(300L), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("available")).isEqualTo(false);
        assertThat(response.getBody().get("reason")).isEqualTo(CarAvailabilityServiceImpl.REASON_SLOT_TAKEN);
    }

    @Test
    @DisplayName("GET /api/v1/bookings/availability returns 400 when endTime is not after startTime")
    void availability_invalidRange_returnsBadRequest() {
        String q = String.format(
                "?carId=400&startTime=%s&endTime=%s",
                windowEnd.toString(),
                windowStart.toString()
        );
        ResponseEntity<Map> response = restTemplate.getForEntity("http://localhost:" + port + "/api/v1/bookings/availability" + q, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private String url(long carId) {
        return "http://localhost:" + port + "/api/v1/bookings/availability?carId=" + carId
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

package com.dynamiccarsharing.booking.controller;

import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"integration", "jpa"})
class BookingControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BookingRepository bookingRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/bookings";
    }

    @Test
    @DisplayName("GET /api/v1/bookings/{id} returns 204 when booking does not exist")
    void getBookingById_whenNotExists_returnsNoContent() {
        ResponseEntity<Map> response = restTemplate.getForEntity(baseUrl + "/999", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("GET /api/v1/bookings/{id} returns 200 and booking when exists")
    void getBookingById_whenExists_returnsOkAndBody() {
        Booking booking = savePendingBooking(1L, 100L, 200L);
        Long id = booking.getId();

        ResponseEntity<Map> response = restTemplate.getForEntity(baseUrl + "/" + id, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("id")).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("PATCH /api/v1/bookings/{id} with APPROVED updates status and returns 200")
    void updateBookingStatus_toApproved_returnsOkAndUpdatedStatus() {
        Booking booking = savePendingBooking(1L, 100L, 200L);
        Long id = booking.getId();

        Map<String, String> body = Map.of("status", "APPROVED");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body);
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/" + id,
                HttpMethod.PATCH,
                request,
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("APPROVED");

        ResponseEntity<Map> getResponse = restTemplate.getForEntity(baseUrl + "/" + id, Map.class);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().get("status")).isEqualTo("APPROVED");
    }

    @Test
    @DisplayName("PATCH /api/v1/bookings/{id} with CANCELED updates status and returns 200")
    void updateBookingStatus_toCanceled_returnsOk() {
        Booking booking = savePendingBooking(1L, 100L, 200L);
        Long id = booking.getId();

        Map<String, String> body = Map.of("status", "CANCELED");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body);
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/" + id,
                HttpMethod.PATCH,
                request,
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("CANCELED");
    }

    @Test
    @DisplayName("DELETE /api/v1/bookings/{id} returns 204 and booking is removed")
    void deleteBooking_whenExists_returnsNoContentAndRemovesBooking() {
        Booking booking = savePendingBooking(1L, 100L, 200L);
        Long id = booking.getId();

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + id,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(bookingRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("GET /api/v1/bookings returns paginated list")
    void getAllBookings_returnsPaginated() {
        savePendingBooking(1L, 10L, 20L);
        savePendingBooking(2L, 11L, 21L);

        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "?page=0&size=10",
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("content")).asList().hasSizeGreaterThanOrEqualTo(2);
    }

    private Booking savePendingBooking(Long renterId, Long carId, Long pickupLocationId) {
        Booking booking = Booking.builder()
                .renterId(renterId)
                .carId(carId)
                .pickupLocationId(pickupLocationId)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .status(TransactionStatus.PENDING)
                .build();
        return bookingRepository.save(booking);
    }
}

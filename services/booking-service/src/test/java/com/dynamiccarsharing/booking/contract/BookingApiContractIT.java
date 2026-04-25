package com.dynamiccarsharing.booking.contract;

import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"integration", "jpa"})
class BookingApiContractIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    @DisplayName("Contract: GET /api/v1/bookings/{id} returns 404 for absent booking")
    void getBookingById_whenMissing_returnsNotFound() {
        ResponseEntity<Map> response = restTemplate.getForEntity("http://localhost:" + port + "/api/v1/bookings/999999", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKeys("title", "status", "detail");
    }

    @Test
    @DisplayName("Contract: GET /api/v1/bookings/{id} returns booking payload")
    void getBookingById_whenExists_returnsBookingPayload() {
        Booking booking = bookingRepository.save(Booking.builder()
                .renterId(701L)
                .carId(702L)
                .pickupLocationId(703L)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .status(TransactionStatus.PENDING)
                .build());

        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/v1/bookings/" + booking.getId(),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKeys("id", "renterId", "carId", "status");
    }
}

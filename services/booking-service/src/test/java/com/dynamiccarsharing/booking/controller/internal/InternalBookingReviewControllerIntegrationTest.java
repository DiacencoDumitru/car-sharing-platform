package com.dynamiccarsharing.booking.controller.internal;

import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"integration", "jpa"})
class InternalBookingReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    void clean() {
        bookingRepository.findAll().forEach(b -> bookingRepository.deleteById(b.getId()));
    }

    @Test
    @DisplayName("GET for-review возвращает данные бронирования")
    void getForReview_returnsBookingSummary() throws Exception {
        Booking saved = bookingRepository.save(Booking.builder()
                .renterId(10L)
                .carId(20L)
                .pickupLocationId(30L)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(2))
                .status(TransactionStatus.COMPLETED)
                .build());

        mockMvc.perform(get("/api/v1/internal/bookings/{bookingId}/for-review", saved.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(saved.getId()))
                .andExpect(jsonPath("$.renterId").value(10))
                .andExpect(jsonPath("$.carId").value(20))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("GET for-review для несуществующего id — 404")
    void getForReview_missing_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/internal/bookings/{bookingId}/for-review", 999_999L))
                .andExpect(status().isNotFound());
    }
}

package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.TestApplication;
import com.dynamiccarsharing.carsharing.dto.BookingDto;
import com.dynamiccarsharing.carsharing.mapper.BookingMapper;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.repository.BookingRepository;
import com.dynamiccarsharing.carsharing.repository.DisputeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingRepository bookingRepository;

    @MockBean
    private DisputeRepository disputeRepository;

    @MockBean
    private BookingMapper bookingMapper;

    @Test
    @WithMockUser
    void getBookingById_whenNotExists_shouldReturnNotFound() throws Exception {
        long nonExistentBookingId = 999L;

        when(bookingRepository.findById(nonExistentBookingId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/bookings/{bookingId}", nonExistentBookingId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getBookingById_whenExists_shouldReturnOk() throws Exception {
        long bookingId = 1L;
        Booking booking = Booking.builder().id(bookingId).build();
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(bookingId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        mockMvc.perform(get("/api/v1/bookings/{bookingId}", bookingId))
                .andExpect(status().isOk());
    }
}
package com.dynamiccarsharing.carsharing.controller;

<<<<<<< HEAD
import com.dynamiccarsharing.carsharing.config.SecurityConfig;
import com.dynamiccarsharing.carsharing.dto.BookingCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.BookingDto;
import com.dynamiccarsharing.carsharing.dto.BookingStatusUpdateRequestDto;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.service.interfaces.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@Import(SecurityConfig.class)
=======
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
>>>>>>> fix/controller-mvc-tests
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
<<<<<<< HEAD
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;
=======
    private BookingRepository bookingRepository;

    @MockBean
    private DisputeRepository disputeRepository;

    @MockBean
    private BookingMapper bookingMapper;
>>>>>>> fix/controller-mvc-tests

    @Test
    @WithMockUser
    void getBookingById_whenNotExists_shouldReturnNotFound() throws Exception {
<<<<<<< HEAD
        when(bookingService.findById(999L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/bookings/{bookingId}", 999L))
=======
        long nonExistentBookingId = 999L;

        when(bookingRepository.findById(nonExistentBookingId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/bookings/{bookingId}", nonExistentBookingId))
>>>>>>> fix/controller-mvc-tests
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getBookingById_whenExists_shouldReturnOk() throws Exception {
<<<<<<< HEAD
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        when(bookingService.findById(1L)).thenReturn(Optional.of(bookingDto));
        mockMvc.perform(get("/api/v1/bookings/{bookingId}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getAllBookings_whenExists_shouldReturnOk() throws Exception {
        BookingDto bookingDto1 = new BookingDto();
        bookingDto1.setId(1L);
        BookingDto bookingDto2 = new BookingDto();
        bookingDto2.setId(2L);
        when(bookingService.findAll()).thenReturn(List.of(bookingDto1, bookingDto2));
        mockMvc.perform(get("/api/v1/bookings"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void createBooking_withValidData_shouldReturnCreated() throws Exception {
        BookingCreateRequestDto createDto = new BookingCreateRequestDto();
        createDto.setRenterId(1L);
        createDto.setCarId(1L);
        createDto.setPickupLocationId(1L);
        LocalDateTime now = LocalDateTime.now();
        createDto.setStartTime(now.plusHours(1));
        createDto.setEndTime(now.plusDays(1));

        BookingDto savedDto = new BookingDto();
        savedDto.setId(1L);
        when(bookingService.save(any(BookingCreateRequestDto.class))).thenReturn(savedDto);

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    void createBooking_withInvalidData_shouldReturnBadRequest() throws Exception {
        BookingCreateRequestDto createDto = new BookingCreateRequestDto();

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void updateBookingStatus_withStatusApproved_shouldReturnOk() throws Exception {
        BookingStatusUpdateRequestDto updateDto = new BookingStatusUpdateRequestDto();
        updateDto.setStatus(TransactionStatus.APPROVED);

        BookingDto updatedDto = new BookingDto();
        updatedDto.setId(1L);
        updatedDto.setStatus(TransactionStatus.APPROVED);

        when(bookingService.approveBooking(1L)).thenReturn(updatedDto);

        mockMvc.perform(patch("/api/v1/bookings/{bookingId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @WithMockUser
    void updateBookingStatus_withStatusCanceled_shouldReturnOk() throws Exception {
        BookingStatusUpdateRequestDto updateDto = new BookingStatusUpdateRequestDto();
        updateDto.setStatus(TransactionStatus.CANCELED);

        when(bookingService.cancelBooking(1L)).thenReturn(new BookingDto());

        mockMvc.perform(patch("/api/v1/bookings/{bookingId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void updateBookingStatus_withStatusCompleted_shouldReturnOk() throws Exception {
        BookingStatusUpdateRequestDto updateDto = new BookingStatusUpdateRequestDto();
        updateDto.setStatus(TransactionStatus.COMPLETED);

        when(bookingService.completeBooking(1L)).thenReturn(new BookingDto());

        mockMvc.perform(patch("/api/v1/bookings/{bookingId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void updateBookingStatus_withInvalidStatus_shouldReturnBadRequest() throws Exception {
        BookingStatusUpdateRequestDto updateDto = new BookingStatusUpdateRequestDto();
        updateDto.setStatus(TransactionStatus.PENDING);

        mockMvc.perform(patch("/api/v1/bookings/{bookingId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deleteBooking_shouldReturnNoContent() throws Exception {
        doNothing().when(bookingService).deleteById(1L);

        mockMvc.perform(delete("/api/v1/bookings/{bookingId}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
=======
        long bookingId = 1L;
        Booking booking = Booking.builder().id(bookingId).build();
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(bookingId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        mockMvc.perform(get("/api/v1/bookings/{bookingId}", bookingId))
                .andExpect(status().isOk());
    }
>>>>>>> fix/controller-mvc-tests
}
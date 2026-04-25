package com.dynamiccarsharing.booking.controller;

import com.dynamiccarsharing.booking.config.SecurityConfig;
import com.dynamiccarsharing.booking.criteria.BookingSearchCriteria;
import com.dynamiccarsharing.booking.dto.BookingCreateRequestDto;
import com.dynamiccarsharing.booking.dto.BookingStatusUpdateRequestDto;
import com.dynamiccarsharing.booking.dto.BookingSummaryResponseDto;
import com.dynamiccarsharing.booking.service.interfaces.BookingService;
import com.dynamiccarsharing.booking.service.interfaces.BookingSummaryService;
import com.dynamiccarsharing.booking.service.interfaces.BookingWaitlistService;
import com.dynamiccarsharing.booking.service.interfaces.CarAvailabilityService;
import com.dynamiccarsharing.booking.service.interfaces.IdempotencyService;
import com.dynamiccarsharing.booking.service.interfaces.QuoteService;
import com.dynamiccarsharing.contracts.dto.BookingDto;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.util.exception.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@Import(SecurityConfig.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;
    @MockBean
    private BookingSummaryService bookingSummaryService;
    @MockBean
    private BookingWaitlistService bookingWaitlistService;
    @MockBean
    private IdempotencyService idempotencyService;
    @MockBean
    private QuoteService quoteService;
    @MockBean
    private CarAvailabilityService carAvailabilityService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void getBookingById_whenNotExists_shouldReturnNotContent() throws Exception {
        when(bookingService.findById(999L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/bookings/{bookingId}", 999L))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void getBookingById_whenExists_shouldReturnOk() throws Exception {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        when(bookingService.findById(1L)).thenReturn(Optional.of(bookingDto));
        mockMvc.perform(get("/api/v1/bookings/{bookingId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser
    void getBookingSummary_whenNotExists_shouldReturnNoContent() throws Exception {
        when(bookingSummaryService.findByBookingId(999L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/bookings/{bookingId}/summary", 999L))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void getBookingSummary_whenExists_shouldReturnOk() throws Exception {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        BookingSummaryResponseDto summary = new BookingSummaryResponseDto();
        summary.setBooking(bookingDto);
        when(bookingSummaryService.findByBookingId(1L)).thenReturn(Optional.of(summary));
        mockMvc.perform(get("/api/v1/bookings/{bookingId}/summary", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.booking.id").value(1L));
    }

    @Test
    @WithMockUser
    void getAllBookings_whenExists_shouldReturnPaginatedBookings() throws Exception {
        BookingDto bookingDto1 = new BookingDto();
        bookingDto1.setId(1L);
        BookingDto bookingDto2 = new BookingDto();
        bookingDto2.setId(2L);
        Page<BookingDto> bookingPage = new PageImpl<>(List.of(bookingDto1, bookingDto2));

        when(bookingService.findAll(any(BookingSearchCriteria.class), any(Pageable.class))).thenReturn(bookingPage);

        mockMvc.perform(get("/api/v1/bookings")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    @WithMockUser
    void createBooking_withValidData_shouldReturnCreated() throws Exception {
        BookingCreateRequestDto createDto = new BookingCreateRequestDto();
        createDto.setRenterId(1L);
        createDto.setCarId(1L);
        createDto.setPickupLocationId(1L);
        createDto.setStartTime(LocalDateTime.now().plusDays(1));
        createDto.setEndTime(LocalDateTime.now().plusDays(2));

        BookingDto savedDto = new BookingDto();
        savedDto.setId(1L);
        when(idempotencyService.execute(any(), any(), any(), any())).thenReturn(savedDto);

        mockMvc.perform(post("/api/v1/bookings")
                        .header("Idempotency-Key", "key-1")
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

        when(bookingService.updateBookingStatus(anyLong(), any(BookingStatusUpdateRequestDto.class))).thenReturn(updatedDto);

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

        BookingDto updatedDto = new BookingDto();
        updatedDto.setId(1L);
        updatedDto.setStatus(TransactionStatus.CANCELED);

        when(bookingService.updateBookingStatus(anyLong(), any(BookingStatusUpdateRequestDto.class))).thenReturn(updatedDto);

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

        BookingDto updatedDto = new BookingDto();
        updatedDto.setId(1L);
        updatedDto.setStatus(TransactionStatus.COMPLETED);

        when(bookingService.updateBookingStatus(anyLong(), any(BookingStatusUpdateRequestDto.class))).thenReturn(updatedDto);

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

        when(bookingService.updateBookingStatus(anyLong(), any(BookingStatusUpdateRequestDto.class)))
                .thenThrow(new ValidationException("Unsupported status for update"));

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
}
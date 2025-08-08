package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.dto.PaymentDto;
import com.dynamiccarsharing.carsharing.dto.PaymentRequestDto;
import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.service.interfaces.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @Test
    @WithMockUser
    void createPayment_withValidData_shouldReturnCreated() throws Exception {
        Long bookingId = 1L;
        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setAmount(new BigDecimal("150.00"));
        requestDto.setPaymentMethod(PaymentType.CREDIT_CARD);

        PaymentDto savedDto = new PaymentDto();
        savedDto.setId(1L);
        savedDto.setBookingId(bookingId);
        savedDto.setStatus(TransactionStatus.PENDING);

        when(paymentService.createPayment(eq(bookingId), any(PaymentRequestDto.class))).thenReturn(savedDto);

        mockMvc.perform(post("/api/v1/bookings/{bookingId}/payment", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPaymentById_whenExists_shouldReturnOk() throws Exception {
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setId(1L);
        when(paymentService.findPaymentById(1L)).thenReturn(Optional.of(paymentDto));

        mockMvc.perform(get("/api/v1/admin/payments/{paymentId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllPayments_shouldReturnList() throws Exception {
        when(paymentService.findAllPayments()).thenReturn(List.of(new PaymentDto(), new PaymentDto()));

        mockMvc.perform(get("/api/v1/admin/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void confirmPayment_whenPending_shouldReturnOk() throws Exception {
        PaymentDto confirmedDto = new PaymentDto();
        confirmedDto.setId(1L);
        confirmedDto.setStatus(TransactionStatus.COMPLETED);

        when(paymentService.confirmPayment(1L)).thenReturn(confirmedDto);

        mockMvc.perform(patch("/api/v1/admin/payments/{paymentId}/confirm", 1L).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletePayment_whenExists_shouldReturnNoContent() throws Exception {
        doNothing().when(paymentService).deleteById(1L);

        mockMvc.perform(delete("/api/v1/admin/payments/{paymentId}", 1L).with(csrf()))
                .andExpect(status().isNoContent());

        verify(paymentService, times(1)).deleteById(1L);
    }
}
package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.TestApplication;
import com.dynamiccarsharing.carsharing.dto.PaymentRequestDto;
import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Payment;
import com.dynamiccarsharing.carsharing.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentRepository paymentRepository;

    @Test
    @WithMockUser
    void createPayment_withValidData_shouldReturnCreated() throws Exception {
        Long bookingId = 1L;
        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setAmount(new BigDecimal("150.00"));
        requestDto.setPaymentMethod(PaymentType.CREDIT_CARD);

        Payment savedPayment = Payment.builder()
                .id(1L)
                .booking(Booking.builder().id(bookingId).build())
                .amount(new BigDecimal("150.00"))
                .paymentMethod(PaymentType.CREDIT_CARD)
                .status(TransactionStatus.PENDING)
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        mockMvc.perform(post("/api/v1/bookings/{bookingId}/payment", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.bookingId").value(bookingId))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPaymentById_whenExists_shouldReturnOk() throws Exception {
        Payment payment = Payment.builder().id(1L).booking(Booking.builder().id(1L).build()).build();
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        mockMvc.perform(get("/api/v1/admin/payments/{paymentId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllPayments_shouldReturnList() throws Exception {
        Payment payment1 = Payment.builder().id(1L).build();
        Payment payment2 = Payment.builder().id(2L).build();
        when(paymentRepository.findAll()).thenReturn(List.of(payment1, payment2));

        mockMvc.perform(get("/api/v1/admin/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void confirmPayment_whenPending_shouldReturnOk() throws Exception {
        Payment pendingPayment = Payment.builder().id(1L).status(TransactionStatus.PENDING).build();
        Payment completedPayment = pendingPayment.withStatus(TransactionStatus.COMPLETED);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(pendingPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(completedPayment);
        
        mockMvc.perform(patch("/api/v1/admin/payments/{paymentId}/confirm", 1L).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletePayment_whenExists_shouldReturnNoContent() throws Exception {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(Payment.builder().id(1L).build()));
        doNothing().when(paymentRepository).deleteById(1L);
        
        mockMvc.perform(delete("/api/v1/admin/payments/{paymentId}", 1L).with(csrf()))
                .andExpect(status().isNoContent());
        
        verify(paymentRepository, times(1)).deleteById(1L);
    }
}
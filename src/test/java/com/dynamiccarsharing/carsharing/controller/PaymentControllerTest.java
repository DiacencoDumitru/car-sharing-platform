package com.dynamiccarsharing.carsharing.controller;

<<<<<<< HEAD
import com.dynamiccarsharing.carsharing.dto.PaymentDto;
import com.dynamiccarsharing.carsharing.dto.PaymentRequestDto;
import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.service.interfaces.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
=======
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
>>>>>>> fix/controller-mvc-tests
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
<<<<<<< HEAD
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
=======
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
>>>>>>> fix/controller-mvc-tests
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
<<<<<<< HEAD
    private PaymentService paymentService;
=======
    private PaymentRepository paymentRepository;
>>>>>>> fix/controller-mvc-tests

    @Test
    @WithMockUser
    void createPayment_withValidData_shouldReturnCreated() throws Exception {
        Long bookingId = 1L;
        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setAmount(new BigDecimal("150.00"));
        requestDto.setPaymentMethod(PaymentType.CREDIT_CARD);

<<<<<<< HEAD
        PaymentDto savedDto = new PaymentDto();
        savedDto.setId(1L);
        savedDto.setBookingId(bookingId);
        savedDto.setStatus(TransactionStatus.PENDING);

        when(paymentService.createPayment(eq(bookingId), any(PaymentRequestDto.class))).thenReturn(savedDto);
=======
        Payment savedPayment = Payment.builder()
                .id(1L)
                .booking(Booking.builder().id(bookingId).build())
                .amount(new BigDecimal("150.00"))
                .paymentMethod(PaymentType.CREDIT_CARD)
                .status(TransactionStatus.PENDING)
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
>>>>>>> fix/controller-mvc-tests

        mockMvc.perform(post("/api/v1/bookings/{bookingId}/payment", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
<<<<<<< HEAD
=======
                .andExpect(jsonPath("$.bookingId").value(bookingId))
>>>>>>> fix/controller-mvc-tests
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPaymentById_whenExists_shouldReturnOk() throws Exception {
<<<<<<< HEAD
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setId(1L);
        when(paymentService.findPaymentById(1L)).thenReturn(Optional.of(paymentDto));
=======
        Payment payment = Payment.builder().id(1L).booking(Booking.builder().id(1L).build()).build();
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
>>>>>>> fix/controller-mvc-tests

        mockMvc.perform(get("/api/v1/admin/payments/{paymentId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }
<<<<<<< HEAD

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllPayments_shouldReturnList() throws Exception {
        when(paymentService.findAllPayments()).thenReturn(List.of(new PaymentDto(), new PaymentDto()));
=======
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllPayments_shouldReturnList() throws Exception {
        Payment payment1 = Payment.builder().id(1L).build();
        Payment payment2 = Payment.builder().id(2L).build();
        when(paymentRepository.findAll()).thenReturn(List.of(payment1, payment2));
>>>>>>> fix/controller-mvc-tests

        mockMvc.perform(get("/api/v1/admin/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void confirmPayment_whenPending_shouldReturnOk() throws Exception {
<<<<<<< HEAD
        PaymentDto confirmedDto = new PaymentDto();
        confirmedDto.setId(1L);
        confirmedDto.setStatus(TransactionStatus.COMPLETED);

        when(paymentService.confirmPayment(1L)).thenReturn(confirmedDto);

=======
        Payment pendingPayment = Payment.builder().id(1L).status(TransactionStatus.PENDING).build();
        Payment completedPayment = pendingPayment.withStatus(TransactionStatus.COMPLETED);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(pendingPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(completedPayment);
        
>>>>>>> fix/controller-mvc-tests
        mockMvc.perform(patch("/api/v1/admin/payments/{paymentId}/confirm", 1L).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletePayment_whenExists_shouldReturnNoContent() throws Exception {
<<<<<<< HEAD
        doNothing().when(paymentService).deleteById(1L);

        mockMvc.perform(delete("/api/v1/admin/payments/{paymentId}", 1L).with(csrf()))
                .andExpect(status().isNoContent());

        verify(paymentService, times(1)).deleteById(1L);
=======
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(Payment.builder().id(1L).build()));
        doNothing().when(paymentRepository).deleteById(1L);
        
        mockMvc.perform(delete("/api/v1/admin/payments/{paymentId}", 1L).with(csrf()))
                .andExpect(status().isNoContent());
        
        verify(paymentRepository, times(1)).deleteById(1L);
>>>>>>> fix/controller-mvc-tests
    }
}
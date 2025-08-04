package com.dynamiccarsharing.carsharing.controller;

<<<<<<< HEAD
import com.dynamiccarsharing.carsharing.dto.TransactionDto;
import com.dynamiccarsharing.carsharing.service.interfaces.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
=======
import com.dynamiccarsharing.carsharing.TestApplication;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Transaction;
import com.dynamiccarsharing.carsharing.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
>>>>>>> fix/controller-mvc-tests
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

<<<<<<< HEAD
@WebMvcTest(TransactionController.class)
=======
@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
>>>>>>> fix/controller-mvc-tests
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
<<<<<<< HEAD
    private TransactionService transactionService;
=======
    private TransactionRepository transactionRepository;
>>>>>>> fix/controller-mvc-tests

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTransactionById_whenExists_shouldReturnOk() throws Exception {
<<<<<<< HEAD
        TransactionDto dto = new TransactionDto();
        dto.setId(1L);
        dto.setBookingId(101L);

        when(transactionService.findTransactionById(1L)).thenReturn(Optional.of(dto));
=======
        Transaction transaction = Transaction.builder()
                .id(1L)
                .booking(Booking.builder().id(101L).build())
                .build();
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
>>>>>>> fix/controller-mvc-tests

        mockMvc.perform(get("/api/v1/admin/transactions/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.bookingId").value(101L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTransactionById_whenNotExists_shouldReturnNotFound() throws Exception {
<<<<<<< HEAD
        when(transactionService.findTransactionById(999L)).thenReturn(Optional.empty());
=======
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());
>>>>>>> fix/controller-mvc-tests

        mockMvc.perform(get("/api/v1/admin/transactions/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllTransactions_shouldReturnList() throws Exception {
<<<<<<< HEAD
        TransactionDto dto1 = new TransactionDto();
        dto1.setId(1L);
        TransactionDto dto2 = new TransactionDto();
        dto2.setId(2L);
        when(transactionService.findAllTransactions()).thenReturn(List.of(dto1, dto2));
=======
        Transaction transaction1 = Transaction.builder().id(1L).build();
        Transaction transaction2 = Transaction.builder().id(2L).build();
        when(transactionRepository.findAll()).thenReturn(List.of(transaction1, transaction2));
>>>>>>> fix/controller-mvc-tests

        mockMvc.perform(get("/api/v1/admin/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L));
    }
}
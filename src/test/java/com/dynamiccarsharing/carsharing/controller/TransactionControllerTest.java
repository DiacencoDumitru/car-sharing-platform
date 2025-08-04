package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.TestApplication;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Transaction;
import com.dynamiccarsharing.carsharing.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionRepository transactionRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTransactionById_whenExists_shouldReturnOk() throws Exception {
        Transaction transaction = Transaction.builder()
                .id(1L)
                .booking(Booking.builder().id(101L).build())
                .build();
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        mockMvc.perform(get("/api/v1/admin/transactions/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.bookingId").value(101L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTransactionById_whenNotExists_shouldReturnNotFound() throws Exception {
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/admin/transactions/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllTransactions_shouldReturnList() throws Exception {
        Transaction transaction1 = Transaction.builder().id(1L).build();
        Transaction transaction2 = Transaction.builder().id(2L).build();
        when(transactionRepository.findAll()).thenReturn(List.of(transaction1, transaction2));

        mockMvc.perform(get("/api/v1/admin/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L));
    }
}
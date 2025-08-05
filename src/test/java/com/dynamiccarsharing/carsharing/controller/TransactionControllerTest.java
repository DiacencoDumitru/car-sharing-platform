package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.dto.TransactionDto;
import com.dynamiccarsharing.carsharing.service.interfaces.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTransactionById_whenExists_shouldReturnOk() throws Exception {
        TransactionDto dto = new TransactionDto();
        dto.setId(1L);
        dto.setBookingId(101L);

        when(transactionService.findTransactionById(1L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/v1/admin/transactions/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.bookingId").value(101L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTransactionById_whenNotExists_shouldReturnNotFound() throws Exception {
        when(transactionService.findTransactionById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/admin/transactions/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllTransactions_shouldReturnList() throws Exception {
        TransactionDto dto1 = new TransactionDto();
        dto1.setId(1L);
        TransactionDto dto2 = new TransactionDto();
        dto2.setId(2L);
        when(transactionService.findAllTransactions()).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/v1/admin/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L));
    }
}
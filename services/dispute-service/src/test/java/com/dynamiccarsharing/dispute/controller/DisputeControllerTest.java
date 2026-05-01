package com.dynamiccarsharing.dispute.controller;

import com.dynamiccarsharing.contracts.dto.DisputeDto;
import com.dynamiccarsharing.contracts.enums.DisputeStatus;
import com.dynamiccarsharing.dispute.config.SecurityConfig;
import com.dynamiccarsharing.dispute.dto.DisputeCreateRequestDto;
import com.dynamiccarsharing.dispute.service.interfaces.DisputeService;
import com.dynamiccarsharing.util.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DisputeController.class)
@Import(SecurityConfig.class)
class DisputeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private DisputeService disputeService;

    @Test
    @WithMockUser(username = "2")
    void createDispute_withValidData_shouldReturnCreated() throws Exception {
        Long bookingId = 1L;
        Long creationUserId = 2L;
        DisputeCreateRequestDto createDto = new DisputeCreateRequestDto();
        createDto.setDescription("The car was not clean.");

        DisputeDto savedDto = new DisputeDto();
        savedDto.setId(1L);
        savedDto.setBookingId(bookingId);
        savedDto.setCreationUserId(creationUserId);
        savedDto.setStatus(DisputeStatus.OPEN);

        when(disputeService.createDispute(eq(bookingId), any(DisputeCreateRequestDto.class), eq(creationUserId)))
                .thenReturn(savedDto);

        mockMvc.perform(post("/api/v1/bookings/{bookingId}/dispute", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllDisputes_shouldReturnList() throws Exception {
        when(disputeService.findAllDisputes()).thenReturn(List.of(new DisputeDto(), new DisputeDto()));

        mockMvc.perform(get("/api/v1/admin/disputes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void resolveDispute_whenExistsAndOpen_shouldReturnOk() throws Exception {
        Long disputeId = 1L;
        DisputeDto resolvedDto = new DisputeDto();
        resolvedDto.setId(disputeId);
        resolvedDto.setStatus(DisputeStatus.RESOLVED);

        when(disputeService.resolveDispute(disputeId)).thenReturn(resolvedDto);

        mockMvc.perform(patch("/api/v1/admin/disputes/{disputeId}/resolve", disputeId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteDispute_whenExists_shouldReturnNoContent() throws Exception {
        Long disputeId = 1L;
        doNothing().when(disputeService).deleteById(disputeId);

        mockMvc.perform(delete("/api/v1/admin/disputes/{disputeId}", disputeId).with(csrf()))
                .andExpect(status().isNoContent());

        verify(disputeService, times(1)).deleteById(disputeId);
    }
}
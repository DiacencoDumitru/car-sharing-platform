package com.dynamiccarsharing.carsharing.controller;

<<<<<<< HEAD
import com.dynamiccarsharing.carsharing.dto.DisputeCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.DisputeDto;
import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.service.interfaces.DisputeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
=======
import com.dynamiccarsharing.carsharing.TestApplication;
import com.dynamiccarsharing.carsharing.dto.DisputeCreateRequestDto;
import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.jpa.DisputeJpaRepository;
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

<<<<<<< HEAD
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DisputeController.class)
=======
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
>>>>>>> fix/controller-mvc-tests
class DisputeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
<<<<<<< HEAD
    private DisputeService disputeService;
=======
    private DisputeJpaRepository disputeJpaRepository;
>>>>>>> fix/controller-mvc-tests

    @Test
    @WithMockUser(username = "2")
    void createDispute_withValidData_shouldReturnCreated() throws Exception {
        Long bookingId = 1L;
<<<<<<< HEAD
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
=======
        DisputeCreateRequestDto createDto = new DisputeCreateRequestDto();
        createDto.setDescription("The car was not clean.");

        Dispute savedDispute = Dispute.builder()
                .id(1L).booking(Booking.builder().id(bookingId).build())
                .creationUser(User.builder().id(2L).build())
                .description("The car was not clean.")
                .status(DisputeStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        when(disputeJpaRepository.save(any(Dispute.class))).thenReturn(savedDispute);
>>>>>>> fix/controller-mvc-tests

        mockMvc.perform(post("/api/v1/bookings/{bookingId}/dispute", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
<<<<<<< HEAD
=======
                .andExpect(jsonPath("$.bookingId").value(bookingId))
                .andExpect(jsonPath("$.creationUserId").value(2L))
>>>>>>> fix/controller-mvc-tests
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllDisputes_shouldReturnList() throws Exception {
<<<<<<< HEAD
        when(disputeService.findAllDisputes()).thenReturn(List.of(new DisputeDto(), new DisputeDto()));
=======
        Dispute dispute1 = Dispute.builder().id(1L).build();
        Dispute dispute2 = Dispute.builder().id(2L).build();
        when(disputeJpaRepository.findAll()).thenReturn(List.of(dispute1, dispute2));
>>>>>>> fix/controller-mvc-tests

        mockMvc.perform(get("/api/v1/admin/disputes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void resolveDispute_whenExistsAndOpen_shouldReturnOk() throws Exception {
        Long disputeId = 1L;
<<<<<<< HEAD
        DisputeDto resolvedDto = new DisputeDto();
        resolvedDto.setId(disputeId);
        resolvedDto.setStatus(DisputeStatus.RESOLVED);

        when(disputeService.resolveDispute(disputeId)).thenReturn(resolvedDto);
=======
        Dispute openDispute = Dispute.builder().id(disputeId).status(DisputeStatus.OPEN).build();
        Dispute resolvedDispute = openDispute.withStatus(DisputeStatus.RESOLVED);
        
        when(disputeJpaRepository.findById(disputeId)).thenReturn(Optional.of(openDispute));
        when(disputeJpaRepository.save(any(Dispute.class))).thenReturn(resolvedDispute);
>>>>>>> fix/controller-mvc-tests

        mockMvc.perform(patch("/api/v1/admin/disputes/{disputeId}/resolve", disputeId)
                        .with(csrf()))
                .andExpect(status().isOk())
<<<<<<< HEAD
=======
                .andExpect(jsonPath("$.id").value(disputeId))
>>>>>>> fix/controller-mvc-tests
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteDispute_whenExists_shouldReturnNoContent() throws Exception {
        Long disputeId = 1L;
<<<<<<< HEAD
        doNothing().when(disputeService).deleteById(disputeId);
=======
        when(disputeJpaRepository.existsById(disputeId)).thenReturn(true);
        doNothing().when(disputeJpaRepository).deleteById(disputeId);
>>>>>>> fix/controller-mvc-tests

        mockMvc.perform(delete("/api/v1/admin/disputes/{disputeId}", disputeId).with(csrf()))
                .andExpect(status().isNoContent());

<<<<<<< HEAD
        verify(disputeService, times(1)).deleteById(disputeId);
=======
        verify(disputeJpaRepository, times(1)).deleteById(disputeId);
>>>>>>> fix/controller-mvc-tests
    }
}
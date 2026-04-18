package com.dynamiccarsharing.car.controller;

import com.dynamiccarsharing.car.criteria.CarSearchCriteria;
import com.dynamiccarsharing.car.dto.CarCreateRequestDto;
import com.dynamiccarsharing.car.dto.CarUpdateRequestDto;
import com.dynamiccarsharing.car.service.interfaces.CarService;
import com.dynamiccarsharing.contracts.dto.CarDto;
import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.contracts.enums.CarType;
import com.dynamiccarsharing.util.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CarController.class)
class CarControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockBean private CarService carService;

    @MockBean private JwtUtil jwtUtil;

    @Test
    @WithMockUser(username = "42")
    void createCar_withValidData_shouldReturnCreated() throws Exception {
        CarCreateRequestDto createDto = new CarCreateRequestDto();
        createDto.setMake("Toyota");
        createDto.setModel("Camry");
        createDto.setRegistrationNumber("ABC-123");
        createDto.setPrice(new BigDecimal("50.00"));
        createDto.setType(CarType.SEDAN);
        createDto.setLocationId(1L);

        CarDto savedCarDto = new CarDto();
        savedCarDto.setId(1L);
        savedCarDto.setMake("Toyota");
        savedCarDto.setStatus(CarStatus.AVAILABLE);

        when(carService.save(any(CarCreateRequestDto.class), eq(42L))).thenReturn(savedCarDto);

        mockMvc.perform(post("/api/v1/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.make").value("Toyota"));

        ArgumentCaptor<CarCreateRequestDto> dtoCaptor = ArgumentCaptor.forClass(CarCreateRequestDto.class);
        ArgumentCaptor<Long> ownerIdCaptor = ArgumentCaptor.forClass(Long.class);
        verify(carService).save(dtoCaptor.capture(), ownerIdCaptor.capture());

        assertEquals(42L, ownerIdCaptor.getValue());
        CarCreateRequestDto passedDto = dtoCaptor.getValue();
        assertEquals("Toyota", passedDto.getMake());
        assertEquals("Camry", passedDto.getModel());
        assertEquals("ABC-123", passedDto.getRegistrationNumber());
        assertEquals(0, new BigDecimal("50.00").compareTo(passedDto.getPrice()));
        assertEquals(CarType.SEDAN, passedDto.getType());
        assertEquals(1L, passedDto.getLocationId());
    }

    @Test
    @WithMockUser
    void createCar_withInvalidData_shouldReturnBadRequest() throws Exception {
        CarCreateRequestDto createDto = new CarCreateRequestDto();

        mockMvc.perform(post("/api/v1/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getCarById_whenExists_shouldReturnOk() throws Exception {
        CarDto carDto = new CarDto();
        carDto.setId(1L);
        carDto.setMake("Honda");
        when(carService.getByIdOrNull(1L)).thenReturn(carDto);

        mockMvc.perform(get("/api/v1/cars/{carId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.make").value("Honda"));
    }

    @Test
    @WithMockUser
    void getCarById_whenNotExists_shouldReturnNoContent() throws Exception {
        when(carService.getByIdOrNull(99L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/cars/{carId}", 99L))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void getAllCars_shouldReturnPaginatedCars() throws Exception {
        CarDto carDto = new CarDto();
        carDto.setId(1L);
        Page<CarDto> carPage = new PageImpl<>(Collections.singletonList(carDto));

        when(carService.findAll(any(CarSearchCriteria.class), any(Pageable.class))).thenReturn(carPage);

        mockMvc.perform(get("/api/v1/cars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = "123")
    void updatedCarDetails_withAuthenticatedUser_shouldReturnOk() throws Exception {
        CarUpdateRequestDto updateDto = new CarUpdateRequestDto();
        updateDto.setModel("Accord");

        CarDto updatedCarDto = new CarDto();
        updatedCarDto.setId(1L);
        updatedCarDto.setModel("Accord");

        when(carService.updateCar(eq(1L), any(CarUpdateRequestDto.class), eq(123L))).thenReturn(updatedCarDto);

        mockMvc.perform(patch("/api/v1/cars/{carId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.model").value("Accord"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void deleteCar_shouldReturnNoContent() throws Exception {
        doNothing().when(carService).deleteById(1L);

        mockMvc.perform(delete("/api/v1/cars/{carId}", 1L).with(csrf()))
                .andExpect(status().isNoContent());
    }
}
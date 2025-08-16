package com.dynamiccarsharing.car.controller;

import com.dynamiccarsharing.car.config.SecurityConfig;
import com.dynamiccarsharing.car.criteria.CarSearchCriteria;
import com.dynamiccarsharing.contracts.dto.CarCreateRequestDto;
import com.dynamiccarsharing.contracts.dto.CarDto;
import com.dynamiccarsharing.contracts.dto.CarUpdateRequestDto;
import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.contracts.enums.CarType;
import com.dynamiccarsharing.car.service.interfaces.CarService;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CarController.class)
@Import(SecurityConfig.class)
class CarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CarService carService;

    @Test
    @WithMockUser
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

        when(carService.save(any(CarCreateRequestDto.class))).thenReturn(savedCarDto);

        mockMvc.perform(post("/api/v1/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.make").value("Toyota"));
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
        when(carService.findById(1L)).thenReturn(Optional.of(carDto));

        mockMvc.perform(get("/api/v1/cars/{carId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.make").value("Honda"));
    }

    @Test
    @WithMockUser
    void getCarById_whenNotExists_shouldReturnNoContent() throws Exception {
        when(carService.findById(99L)).thenReturn(Optional.empty());

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
    @WithMockUser
    void updatedCarDetails_shouldReturnOk() throws Exception {
        CarUpdateRequestDto updateDto = new CarUpdateRequestDto();
        updateDto.setModel("Accord");

        CarDto updatedCarDto = new CarDto();
        updatedCarDto.setId(1L);
        updatedCarDto.setModel("Accord");

        when(carService.updateCar(eq(1L), any(CarUpdateRequestDto.class))).thenReturn(updatedCarDto);

        mockMvc.perform(patch("/api/v1/cars/{carId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.model").value("Accord"));
    }

    @Test
    @WithMockUser
    void deleteCar_shouldReturnNoContent() throws Exception {
        doNothing().when(carService).deleteById(1L);

        mockMvc.perform(delete("/api/v1/cars/{carId}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.dto.CarCreateRequestDto;
import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.CarType;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.service.interfaces.CarService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CarController.class)
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

        Car savedCar = Car.builder()
                .id(1L)
                .make("Toyota").model("Camry").registrationNumber("ABC-123")
                .price(new BigDecimal("50.00")).type(CarType.SEDAN)
                .location(Location.builder().id(1L).build())
                .status(CarStatus.AVAILABLE)
                .verificationStatus(VerificationStatus.PENDING)
                .build();

        when(carService.save(any(Car.class))).thenReturn(savedCar);

        mockMvc.perform(post("/api/v1/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.make").value("Toyota"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }
}

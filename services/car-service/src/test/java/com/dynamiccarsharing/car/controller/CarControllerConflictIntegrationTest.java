package com.dynamiccarsharing.car.controller;

import com.dynamiccarsharing.car.dto.CarUpdateRequestDto;
import com.dynamiccarsharing.car.exception.handler.GlobalExceptionHandler;
import com.dynamiccarsharing.car.service.interfaces.CarService;
import com.dynamiccarsharing.util.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CarController.class)
@AutoConfigureMockMvc
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = "eureka.instance.instance-id=car-service:test")
class CarControllerConflictIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CarService carService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void updateCar_returnsConflict_whenOptimisticLockingFailureHappens() throws Exception {
        Long carId = 42L;
        Long ownerId = 1L;

        when(carService.updateCar(anyLong(), any(CarUpdateRequestDto.class), anyLong()))
                .thenThrow(new ObjectOptimisticLockingFailureException(CarController.class, carId));

        CarUpdateRequestDto request = new CarUpdateRequestDto();
        request.setModel("Prius");

        mockMvc.perform(patch("/api/v1/cars/{carId}", carId)
                        .with(SecurityMockMvcRequestPostProcessors.user(ownerId.toString()))
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Concurrent Update Conflict"))
                .andExpect(jsonPath("$.type").value("/errors/concurrent-update-conflict"));
    }
}

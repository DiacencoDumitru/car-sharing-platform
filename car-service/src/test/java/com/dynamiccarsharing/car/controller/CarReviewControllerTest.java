package com.dynamiccarsharing.car.controller;

import com.dynamiccarsharing.contracts.dto.CarReviewCreateRequestDto;
import com.dynamiccarsharing.contracts.dto.CarReviewDto;
import com.dynamiccarsharing.contracts.dto.CarReviewUpdateRequestDto;
import com.dynamiccarsharing.car.service.interfaces.CarReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CarReviewController.class)
class CarReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CarReviewService carReviewService;

    @Test
    @WithMockUser
    void createCarReview_withValidData_shouldReturnCreated() throws Exception {
        Long carId = 1L;
        CarReviewCreateRequestDto createDto = new CarReviewCreateRequestDto();
        createDto.setReviewerId(2L);
        createDto.setComment("Great car!");

        CarReviewDto savedDto = new CarReviewDto();
        savedDto.setId(101L);
        savedDto.setCarId(carId);
        savedDto.setReviewerId(2L);
        savedDto.setComment("Great car!");

        when(carReviewService.createReview(eq(carId), any(CarReviewCreateRequestDto.class))).thenReturn(savedDto);

        mockMvc.perform(post("/api/v1/cars/{carId}/reviews", carId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(101L))
                .andExpect(jsonPath("$.carId").value(carId))
                .andExpect(jsonPath("$.reviewerId").value(2L))
                .andExpect(jsonPath("$.comment").value("Great car!"));
    }

    @Test
    @WithMockUser
    void getReviewsForCar_shouldReturnReviewList() throws Exception {
        Long carId = 1L;
        CarReviewDto review1 = new CarReviewDto();
        review1.setId(101L);
        CarReviewDto review2 = new CarReviewDto();
        review2.setId(102L);
        when(carReviewService.findByCarId(carId)).thenReturn(List.of(review1, review2));

        mockMvc.perform(get("/api/v1/cars/{carId}/reviews", carId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(101L));
    }

    @Test
    @WithMockUser
    void getReviewById_whenExists_shouldReturnOk() throws Exception {
        Long reviewId = 101L;
        CarReviewDto reviewDto = new CarReviewDto();
        reviewDto.setId(reviewId);
        when(carReviewService.findById(reviewId)).thenReturn(Optional.of(reviewDto));

        mockMvc.perform(get("/api/v1/car-reviews/{reviewId}", reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId));
    }

    @Test
    @WithMockUser
    void getReviewById_whenNotExists_shouldReturnNoContent() throws Exception {
        when(carReviewService.findById(999L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/car-reviews/{reviewId}", 999L))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void updateReview_whenExists_shouldReturnOk() throws Exception {
        Long reviewId = 101L;
        CarReviewUpdateRequestDto updateDto = new CarReviewUpdateRequestDto();
        updateDto.setComment("New comment");

        CarReviewDto updatedDto = new CarReviewDto();
        updatedDto.setId(reviewId);
        updatedDto.setComment("New comment");

        when(carReviewService.updateReview(eq(reviewId), any(CarReviewUpdateRequestDto.class))).thenReturn(updatedDto);

        mockMvc.perform(patch("/api/v1/car-reviews/{reviewId}", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.comment").value("New comment"));
    }

    @Test
    @WithMockUser
    void deleteReview_whenExists_shouldReturnNoContent() throws Exception {
        Long reviewId = 101L;
        doNothing().when(carReviewService).deleteById(reviewId);

        mockMvc.perform(delete("/api/v1/car-reviews/{reviewId}", reviewId).with(csrf()))
                .andExpect(status().isNoContent());

        verify(carReviewService, times(1)).deleteById(reviewId);
    }
}
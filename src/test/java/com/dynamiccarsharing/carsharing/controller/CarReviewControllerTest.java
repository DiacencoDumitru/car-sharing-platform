package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.TestApplication;
import com.dynamiccarsharing.carsharing.dto.CarReviewCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.CarReviewUpdateRequestDto;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.CarReview;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.CarReviewRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
class CarReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CarReviewRepository carReviewRepository;

    @Test
    @WithMockUser
    void createCarReview_withValidData_shouldReturnCreated() throws Exception {
        Long carId = 1L;
        CarReviewCreateRequestDto createDto = new CarReviewCreateRequestDto();
        createDto.setReviewerId(2L);
        createDto.setComment("Great car!");

        CarReview savedReview = CarReview.builder()
                .id(101L)
                .car(Car.builder().id(carId).build())
                .reviewer(User.builder().id(2L).build())
                .comment("Great car!")
                .build();

        when(carReviewRepository.save(any(CarReview.class))).thenReturn(savedReview);

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
        CarReview review1 = CarReview.builder().id(101L).car(Car.builder().id(carId).build()).build();
        CarReview review2 = CarReview.builder().id(102L).car(Car.builder().id(carId).build()).build();
        when(carReviewRepository.findByCarId(carId)).thenReturn(List.of(review1, review2));

        mockMvc.perform(get("/api/v1/cars/{carId}/reviews", carId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(101L));
    }

    @Test
    @WithMockUser
    void getReviewById_whenExists_shouldReturnOk() throws Exception {
        Long reviewId = 101L;
        CarReview review = CarReview.builder()
                .id(reviewId)
                .car(Car.builder().id(1L).build())
                .reviewer(User.builder().id(2L).build())
                .comment("Test comment")
                .build();
        when(carReviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        mockMvc.perform(get("/api/v1/reviews/{reviewId}", reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId));
    }

    @Test
    @WithMockUser
    void getReviewById_whenNotExists_shouldReturnNotFound() throws Exception {
        when(carReviewRepository.findById(999L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/reviews/{reviewId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void updateReview_whenExists_shouldReturnOk() throws Exception {
        Long reviewId = 101L;
        CarReview existingReview = CarReview.builder().id(reviewId).comment("Old comment").build();
        CarReview updatedReview = existingReview.withComment("New comment");

        CarReviewUpdateRequestDto updateDto = new CarReviewUpdateRequestDto();
        updateDto.setComment("New comment");

        when(carReviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));
        when(carReviewRepository.save(any(CarReview.class))).thenReturn(updatedReview);

        mockMvc.perform(patch("/api/v1/reviews/{reviewId}", reviewId)
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
        when(carReviewRepository.findById(reviewId)).thenReturn(Optional.of(CarReview.builder().id(reviewId).build()));
        doNothing().when(carReviewRepository).deleteById(reviewId);

        mockMvc.perform(delete("/api/v1/reviews/{reviewId}", reviewId).with(csrf()))
                .andExpect(status().isNoContent());

        verify(carReviewRepository, times(1)).deleteById(reviewId);
    }
}
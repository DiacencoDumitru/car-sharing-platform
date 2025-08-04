package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.TestApplication;
import com.dynamiccarsharing.carsharing.dto.UserReviewCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.UserReviewUpdateRequestDto;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.model.UserReview;
import com.dynamiccarsharing.carsharing.repository.UserReviewRepository;
import com.dynamiccarsharing.carsharing.repository.jpa.UserReviewJpaRepository;
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
class UserReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ObjectMapper objectMapper;

    @MockBean
    private UserReviewJpaRepository userReviewRepository;

    @Test
    @WithMockUser
    void createUserReview_withValidData_shouldReturnCreated() throws Exception {
        Long userId = 1L;
        Long reviewerId = 2L;

        UserReviewCreateRequestDto createDto = new UserReviewCreateRequestDto();
        createDto.setReviewerId(reviewerId);
        createDto.setComment("Very friendly user.");

        UserReview savedReview = UserReview.builder()
                .id(1L)
                .user(User.builder().id(userId).build())
                .reviewer(User.builder().id(reviewerId).build())
                .comment("Very test friendly user.")
                .build();

        when(userReviewRepository.save(any(UserReview.class))).thenReturn(savedReview);

        mockMvc.perform(post("/api/v1/users/{userId}/reviews", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.reviewerId").value(reviewerId))
                .andExpect(jsonPath("$.comment").value("Very friendly user."));
    }

    @Test
    @WithMockUser
    void getReviewsForUser_shouldReturnReviewList() throws Exception {
        Long userId = 1L;
        UserReview review1 = UserReview.builder().id(101L).user(User.builder().id(userId).build()).build();
        UserReview review2 = UserReview.builder().id(102L).user(User.builder().id(userId).build()).build();
        when(userReviewRepository.findByUserId(userId)).thenReturn(List.of(review1, review2));

        mockMvc.perform(get("/api/v1/users/{userId}/reviews", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser
    void getReviewById_whenExists_shouldReturnOk() throws Exception {
        Long reviewId = 101L;
        UserReview review = UserReview.builder().id(reviewId).user(User.builder().id(1L).build()).build();
        when(userReviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        mockMvc.perform(get("/api/v1/reviews/{reviewId}", reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId));
    }

    @Test
    @WithMockUser
    void updateReview_whenExists_shouldReturnOk() throws Exception {
        Long reviewId = 101L;
        UserReview existingReview = UserReview.builder().id(reviewId).comment("Old comment").build();
        UserReview updatedReview = existingReview.withComment("New comment");

        UserReviewUpdateRequestDto updateDto = new UserReviewUpdateRequestDto();
        updateDto.setComment("New comment");

        when(userReviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));
        when(userReviewRepository.save(any(UserReview.class))).thenReturn(updatedReview);

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
        when(userReviewRepository.findById(reviewId)).thenReturn(Optional.of(UserReview.builder().id(reviewId).build()));
        doNothing().when(userReviewRepository).deleteById(reviewId);

        mockMvc.perform(delete("/api/v1/reviews/{reviewId}", reviewId).with(csrf()))
                .andExpect(status().isNoContent());

        verify(userReviewRepository, times(1)).deleteById(reviewId);
    }
}
package com.dynamiccarsharing.user.controller;

import com.dynamiccarsharing.user.dto.UserReviewCreateRequestDto;
import com.dynamiccarsharing.user.dto.UserReviewDto;
import com.dynamiccarsharing.user.dto.UserReviewUpdateRequestDto;
import com.dynamiccarsharing.user.service.interfaces.UserReviewService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserReviewController.class)
class UserReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserReviewService userReviewService;

    @Test
    @WithMockUser
    void createUserReview_withValidData_shouldReturnCreated() throws Exception {
        Long userId = 1L;
        UserReviewCreateRequestDto createDto = new UserReviewCreateRequestDto();
        createDto.setReviewerId(2L);
        createDto.setComment("Very friendly user.");

        UserReviewDto savedDto = new UserReviewDto();
        savedDto.setId(1L);
        savedDto.setUserId(userId);

        when(userReviewService.createUserReview(eq(userId), any(UserReviewCreateRequestDto.class))).thenReturn(savedDto);

        mockMvc.perform(post("/api/v1/users/{userId}/reviews", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(userId));
    }

    @Test
    @WithMockUser
    void getReviewsForUser_shouldReturnReviewList() throws Exception {
        Long userId = 1L;
        when(userReviewService.findReviewsByUserId(userId)).thenReturn(List.of(new UserReviewDto(), new UserReviewDto()));

        mockMvc.perform(get("/api/v1/users/{userId}/reviews", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser
    void getReviewById_whenExists_shouldReturnOk() throws Exception {
        Long reviewId = 101L;
        UserReviewDto dto = new UserReviewDto();
        dto.setId(reviewId);
        when(userReviewService.findReviewById(reviewId)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/v1/user-reviews/{reviewId}", reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId));
    }

    @Test
    @WithMockUser
    void updateReview_whenExists_shouldReturnOk() throws Exception {
        Long reviewId = 101L;
        UserReviewUpdateRequestDto updateDto = new UserReviewUpdateRequestDto();
        updateDto.setComment("New comment");

        UserReviewDto updatedDto = new UserReviewDto();
        updatedDto.setId(reviewId);
        updatedDto.setComment("New comment");

        when(userReviewService.updateReview(eq(reviewId), any(UserReviewUpdateRequestDto.class))).thenReturn(updatedDto);

        mockMvc.perform(patch("/api/v1/user-reviews/{reviewId}", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").value("New comment"));
    }

    @Test
    @WithMockUser
    void deleteReview_whenExists_shouldReturnNoContent() throws Exception {
        Long reviewId = 101L;
        doNothing().when(userReviewService).deleteById(reviewId);

        mockMvc.perform(delete("/api/v1/user-reviews/{reviewId}", reviewId).with(csrf()))
                .andExpect(status().isNoContent());

        verify(userReviewService, times(1)).deleteById(reviewId);
    }
}
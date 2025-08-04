package com.dynamiccarsharing.carsharing.controller;

<<<<<<< HEAD
import com.dynamiccarsharing.carsharing.dto.UserReviewCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.UserReviewDto;
import com.dynamiccarsharing.carsharing.dto.UserReviewUpdateRequestDto;
import com.dynamiccarsharing.carsharing.service.interfaces.UserReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
=======
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
>>>>>>> fix/controller-mvc-tests
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
<<<<<<< HEAD
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserReviewController.class)
=======
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
>>>>>>> fix/controller-mvc-tests
class UserReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

<<<<<<< HEAD
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserReviewService userReviewService;
=======
    @MockBean
    private ObjectMapper objectMapper;

    @MockBean
    private UserReviewJpaRepository userReviewRepository;
>>>>>>> fix/controller-mvc-tests

    @Test
    @WithMockUser
    void createUserReview_withValidData_shouldReturnCreated() throws Exception {
        Long userId = 1L;
<<<<<<< HEAD
        UserReviewCreateRequestDto createDto = new UserReviewCreateRequestDto();
        createDto.setReviewerId(2L);
        createDto.setComment("Very friendly user.");

        UserReviewDto savedDto = new UserReviewDto();
        savedDto.setId(1L);
        savedDto.setUserId(userId);

        when(userReviewService.createUserReview(eq(userId), any(UserReviewCreateRequestDto.class))).thenReturn(savedDto);
=======
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
>>>>>>> fix/controller-mvc-tests

        mockMvc.perform(post("/api/v1/users/{userId}/reviews", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
<<<<<<< HEAD
                .andExpect(jsonPath("$.userId").value(userId));
=======
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.reviewerId").value(reviewerId))
                .andExpect(jsonPath("$.comment").value("Very friendly user."));
>>>>>>> fix/controller-mvc-tests
    }

    @Test
    @WithMockUser
    void getReviewsForUser_shouldReturnReviewList() throws Exception {
        Long userId = 1L;
<<<<<<< HEAD
        when(userReviewService.findReviewsByUserId(userId)).thenReturn(List.of(new UserReviewDto(), new UserReviewDto()));
=======
        UserReview review1 = UserReview.builder().id(101L).user(User.builder().id(userId).build()).build();
        UserReview review2 = UserReview.builder().id(102L).user(User.builder().id(userId).build()).build();
        when(userReviewRepository.findByUserId(userId)).thenReturn(List.of(review1, review2));
>>>>>>> fix/controller-mvc-tests

        mockMvc.perform(get("/api/v1/users/{userId}/reviews", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser
    void getReviewById_whenExists_shouldReturnOk() throws Exception {
        Long reviewId = 101L;
<<<<<<< HEAD
        UserReviewDto dto = new UserReviewDto();
        dto.setId(reviewId);
        when(userReviewService.findReviewById(reviewId)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/v1/user-reviews/{reviewId}", reviewId))
=======
        UserReview review = UserReview.builder().id(reviewId).user(User.builder().id(1L).build()).build();
        when(userReviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        mockMvc.perform(get("/api/v1/reviews/{reviewId}", reviewId))
>>>>>>> fix/controller-mvc-tests
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId));
    }

    @Test
    @WithMockUser
    void updateReview_whenExists_shouldReturnOk() throws Exception {
        Long reviewId = 101L;
<<<<<<< HEAD
        UserReviewUpdateRequestDto updateDto = new UserReviewUpdateRequestDto();
        updateDto.setComment("New comment");

        UserReviewDto updatedDto = new UserReviewDto();
        updatedDto.setId(reviewId);
        updatedDto.setComment("New comment");

        when(userReviewService.updateReview(eq(reviewId), any(UserReviewUpdateRequestDto.class))).thenReturn(updatedDto);

        mockMvc.perform(patch("/api/v1/user-reviews/{reviewId}", reviewId)
=======
        UserReview existingReview = UserReview.builder().id(reviewId).comment("Old comment").build();
        UserReview updatedReview = existingReview.withComment("New comment");

        UserReviewUpdateRequestDto updateDto = new UserReviewUpdateRequestDto();
        updateDto.setComment("New comment");

        when(userReviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));
        when(userReviewRepository.save(any(UserReview.class))).thenReturn(updatedReview);

        mockMvc.perform(patch("/api/v1/reviews/{reviewId}", reviewId)
>>>>>>> fix/controller-mvc-tests
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .with(csrf()))
                .andExpect(status().isOk())
<<<<<<< HEAD
=======
                .andExpect(jsonPath("$.id").value(reviewId))
>>>>>>> fix/controller-mvc-tests
                .andExpect(jsonPath("$.comment").value("New comment"));
    }

    @Test
    @WithMockUser
    void deleteReview_whenExists_shouldReturnNoContent() throws Exception {
        Long reviewId = 101L;
<<<<<<< HEAD
        doNothing().when(userReviewService).deleteById(reviewId);

        mockMvc.perform(delete("/api/v1/user-reviews/{reviewId}", reviewId).with(csrf()))
                .andExpect(status().isNoContent());

        verify(userReviewService, times(1)).deleteById(reviewId);
=======
        when(userReviewRepository.findById(reviewId)).thenReturn(Optional.of(UserReview.builder().id(reviewId).build()));
        doNothing().when(userReviewRepository).deleteById(reviewId);

        mockMvc.perform(delete("/api/v1/reviews/{reviewId}", reviewId).with(csrf()))
                .andExpect(status().isNoContent());

        verify(userReviewRepository, times(1)).deleteById(reviewId);
>>>>>>> fix/controller-mvc-tests
    }
}
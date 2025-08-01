package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.TestApplication;
import com.dynamiccarsharing.carsharing.dto.ContactInfoCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.UserCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.UserStatusUpdateRequestDto;
import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.CarRepository;
import com.dynamiccarsharing.carsharing.repository.UserRepository;
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
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CarRepository carRepository; // Dependency of UserServiceImpl

    @Test
    void registerUser_withValidData_shouldReturnCreated() throws Exception {
        ContactInfoCreateRequestDto contactDto = new ContactInfoCreateRequestDto();
        contactDto.setFirstName("Jane");
        contactDto.setLastName("Doe");
        contactDto.setEmail("jane.doe@example.com");
        contactDto.setPhoneNumber("555-0101");

        UserCreateRequestDto createDto = new UserCreateRequestDto();
        createDto.setRole(UserRole.RENTER);
        createDto.setContactInfo(contactDto);

        User savedUser = User.builder()
                .id(1L).role(UserRole.RENTER).status(UserStatus.ACTIVE)
                .contactInfo(ContactInfo.builder().id(10L).email("jane.doe@example.com").build())
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.role").value("RENTER"))
                .andExpect(jsonPath("$.contactInfo.email").value("jane.doe@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_asAdmin_shouldReturnUserList() throws Exception {
        User user1 = User.builder().id(1L).build();
        User user2 = User.builder().id(2L).build();
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserStatus_asAdmin_shouldReturnOk() throws Exception {
        User existingUser = User.builder().id(1L).status(UserStatus.ACTIVE).build();
        User updatedUser = existingUser.withStatus(UserStatus.SUSPENDED);

        UserStatusUpdateRequestDto updateDto = new UserStatusUpdateRequestDto();
        updateDto.setStatus(UserStatus.SUSPENDED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(patch("/api/v1/users/{userId}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_asAdmin_shouldReturnNoContent() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        doNothing().when(userRepository).deleteById(1L);

        mockMvc.perform(delete("/api/v1/users/{userId}", 1L).with(csrf()))
                .andExpect(status().isNoContent());

        verify(userRepository, times(1)).deleteById(1L);
    }
}
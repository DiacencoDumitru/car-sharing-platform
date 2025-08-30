package com.dynamiccarsharing.user.controller;

import com.dynamiccarsharing.contracts.dto.UserDto;
import com.dynamiccarsharing.contracts.enums.UserRole;
import com.dynamiccarsharing.contracts.enums.UserStatus;
import com.dynamiccarsharing.user.dto.ContactInfoCreateRequestDto;
import com.dynamiccarsharing.user.dto.UserCreateRequestDto;
import com.dynamiccarsharing.user.dto.UserStatusUpdateRequestDto;
import com.dynamiccarsharing.user.service.interfaces.UserService;
import com.dynamiccarsharing.util.security.JwtAuthenticationFilter;
import com.dynamiccarsharing.util.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = UserController.class, properties = "eureka.instance.instance-id=test-instance-1")
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    AuthenticationProvider authenticationProvider;

    @BeforeEach
    void passThroughFilter() throws Exception {
        doAnswer(inv -> {
            var request = inv.getArgument(0, ServletRequest.class);
            var response = inv.getArgument(1, ServletResponse.class);
            var filterChain = inv.getArgument(2, FilterChain.class);
            filterChain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(
                any(ServletRequest.class),
                any(ServletResponse.class),
                any(FilterChain.class)
        );
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void registerUser_withValidData_shouldReturnCreated() throws Exception {
        var contactInfoDto = new ContactInfoCreateRequestDto();
        contactInfoDto.setFirstName("Dumitru");
        contactInfoDto.setLastName("Diacenco");
        contactInfoDto.setEmail("dd.prodev@gmail.com");
        contactInfoDto.setPhoneNumber("+37367773888");
        contactInfoDto.setPassword("password123");

        var createDto = new UserCreateRequestDto();
        createDto.setRole(UserRole.RENTER);
        createDto.setContactInfo(contactInfoDto);

        var savedDto = new UserDto();
        savedDto.setId(1L);

        when(userService.registerUser(any(UserCreateRequestDto.class))).thenReturn(savedDto);

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getAllUsers_asAdmin_shouldReturnUserList() throws Exception {
        when(userService.findAllUsers()).thenReturn(List.of(new UserDto(), new UserDto()));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void updateUserStatus_asAdmin_shouldReturnOk() throws Exception {
        var updateDto = new UserStatusUpdateRequestDto();
        updateDto.setStatus(UserStatus.SUSPENDED);

        var updatedDto = new UserDto();
        updatedDto.setId(1L);
        updatedDto.setStatus(UserStatus.SUSPENDED);

        when(userService.updateUserStatus(eq(1L), any(UserStatusUpdateRequestDto.class))).thenReturn(updatedDto);

        mockMvc.perform(patch("/api/v1/users/{userId}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void deleteUser_asAdmin_shouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteById(1L);

        mockMvc.perform(delete("/api/v1/users/{userId}", 1L).with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteById(1L);
    }
}

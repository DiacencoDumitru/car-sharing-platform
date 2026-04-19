package com.dynamiccarsharing.user.controller;

import com.dynamiccarsharing.contracts.dto.ContactInfoDto;
import com.dynamiccarsharing.contracts.dto.UserDto;
import com.dynamiccarsharing.contracts.enums.UserRole;
import com.dynamiccarsharing.contracts.enums.UserStatus;
import com.dynamiccarsharing.user.config.SecurityConfig;
import com.dynamiccarsharing.user.security.InternalApiKeyAuthenticationFilter;
import com.dynamiccarsharing.user.dto.ContactInfoCreateRequestDto;
import com.dynamiccarsharing.user.dto.ContactInfoUpdateRequestDto;
import com.dynamiccarsharing.user.dto.UserCreateRequestDto;
import com.dynamiccarsharing.user.dto.UserStatusUpdateRequestDto;
import com.dynamiccarsharing.user.service.UserServiceImpl;
import com.dynamiccarsharing.util.security.JwtAuthenticationFilter;
import com.dynamiccarsharing.util.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = UserController.class, properties = {
        "eureka.instance.instance-id=test-instance-1",
        "application.security.internal-api-key=test-internal-api-key-for-integration"
})
@Import({SecurityConfig.class, InternalApiKeyAuthenticationFilter.class, UserControllerTest.PassThroughJwtFilterConfig.class})
@ActiveProfiles("test")
class UserControllerTest {

    private static final String VALID_INTERNAL_API_KEY = "test-internal-api-key-for-integration";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    private UserServiceImpl userServiceImpl;

    @TestConfiguration
    static class PassThroughJwtFilterConfig {
        @Bean
        @Primary
        JwtUtil jwtUtil() {
            JwtUtil jwtUtil = new JwtUtil();
            ReflectionTestUtils.setField(jwtUtil, "secretKey",
                    "NDM0YjVmNjM1MTY0NWE3NDc3Mzg3YTMzNjE1MTQxNTg0NTUxNDg0ZjU0N2IzYjRmNmE1YjdhNmE0ZDQxNjM1Mg==");
            return jwtUtil;
        }

        @Bean
        @Primary
        JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil) {
            return new JwtAuthenticationFilter(jwtUtil) {
                @Override
                protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                        throws ServletException, IOException {
                    filterChain.doFilter(request, response);
                }
            };
        }
    }

    @Test
    @WithMockUser
    void getUserById_whenUserExists_shouldReturnUser() throws Exception {
        ContactInfoDto contactDto = new ContactInfoDto();
        contactDto.setEmail("test@example.com");
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setContactInfo(contactDto);

        when(userServiceImpl.findUserById(1L)).thenReturn(Optional.of(userDto));

        mockMvc.perform(get("/api/v1/users/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.contactInfo.email").value("test@example.com"))
                .andExpect(jsonPath("$.instanceId").value("test-instance-1"));
    }

    @Test
    @WithMockUser
    void getUserById_whenUserNotExists_shouldReturnNoContent() throws Exception {
        when(userServiceImpl.findUserById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/users/{userId}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "123")
    void getCurrentUserProfile_whenUserExists_shouldReturnProfile() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setId(123L);

        when(userServiceImpl.findUserById(123L)).thenReturn(Optional.of(userDto));

        mockMvc.perform(get("/api/v1/profile/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(123L));
    }

    @Test
    @WithMockUser(username = "123")
    void getCurrentUserProfile_whenUserNotExists_shouldReturnNoContent() throws Exception {
        when(userServiceImpl.findUserById(123L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/profile/me"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "123")
    void updateCurrentUserContactInfo_shouldReturnUpdatedUser() throws Exception {
        ContactInfoUpdateRequestDto updateDto = new ContactInfoUpdateRequestDto();
        updateDto.setPhoneNumber("111-222-333");

        ContactInfoDto contactDto = new ContactInfoDto();
        contactDto.setPhoneNumber("111-222-333");
        UserDto updatedUser = new UserDto();
        updatedUser.setId(123L);
        updatedUser.setContactInfo(contactDto);

        when(userServiceImpl.updateUserContactInfo(eq(123L), any(ContactInfoUpdateRequestDto.class)))
                .thenReturn(updatedUser);

        mockMvc.perform(patch("/api/v1/profile/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(123L))
                .andExpect(jsonPath("$.contactInfo.phoneNumber").value("111-222-333"));
    }

    @Test
    void getUserByEmailForService_whenUserExists_shouldReturnUser() throws Exception {
        String email = "internal@example.com";
        ContactInfoDto contactDto = new ContactInfoDto();
        contactDto.setEmail(email);
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setContactInfo(contactDto);

        when(userServiceImpl.findByEmail(email)).thenReturn(Optional.of(userDto));

        mockMvc.perform(get("/api/v1/internal/users/by-email/{email}", email)
                        .header(InternalApiKeyAuthenticationFilter.INTERNAL_API_KEY_HEADER, VALID_INTERNAL_API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactInfo.email").value(email));
    }

    @Test
    void getUserByEmailForService_whenUserNotExists_shouldReturnNotFound() throws Exception {
        String email = "notfound@example.com";
        when(userServiceImpl.findByEmail(email)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/internal/users/by-email/{email}", email)
                        .header(InternalApiKeyAuthenticationFilter.INTERNAL_API_KEY_HEADER, VALID_INTERNAL_API_KEY))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserByEmailForService_whenApiKeyMissing_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/internal/users/by-email/{email}", "any@example.com"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserByEmailForService_whenApiKeyInvalid_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/internal/users/by-email/{email}", "any@example.com")
                        .header(InternalApiKeyAuthenticationFilter.INTERNAL_API_KEY_HEADER, "wrong-key"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserByIdForService_whenUserExists_shouldReturnUser() throws Exception {
        ContactInfoDto contactDto = new ContactInfoDto();
        contactDto.setEmail("svc@example.com");
        UserDto userDto = new UserDto();
        userDto.setId(2L);
        userDto.setContactInfo(contactDto);

        when(userServiceImpl.findUserById(2L)).thenReturn(Optional.of(userDto));

        mockMvc.perform(get("/api/v1/internal/users/{userId}", 2L)
                        .header(InternalApiKeyAuthenticationFilter.INTERNAL_API_KEY_HEADER, VALID_INTERNAL_API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.contactInfo.email").value("svc@example.com"))
                .andExpect(jsonPath("$.instanceId").value("test-instance-1"));
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

        when(userServiceImpl.registerUser(any(UserCreateRequestDto.class))).thenReturn(savedDto);

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
        when(userServiceImpl.findAllUsers()).thenReturn(List.of(new UserDto(), new UserDto()));

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

        when(userServiceImpl.updateUserStatus(eq(1L), any(UserStatusUpdateRequestDto.class))).thenReturn(updatedDto);

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
        doNothing().when(userServiceImpl).deleteById(1L);

        mockMvc.perform(delete("/api/v1/users/{userId}", 1L).with(csrf()))
                .andExpect(status().isNoContent());

        verify(userServiceImpl, times(1)).deleteById(1L);
    }
}
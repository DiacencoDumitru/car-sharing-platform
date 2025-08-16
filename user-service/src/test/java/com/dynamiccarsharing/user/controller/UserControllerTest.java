package com.dynamiccarsharing.user.controller;

import com.dynamiccarsharing.user.config.SecurityConfig;
import com.dynamiccarsharing.contracts.dto.ContactInfoCreateRequestDto;
import com.dynamiccarsharing.contracts.dto.UserCreateRequestDto;
import com.dynamiccarsharing.contracts.dto.UserDto;
import com.dynamiccarsharing.contracts.dto.UserStatusUpdateRequestDto;
import com.dynamiccarsharing.contracts.enums.UserRole;
import com.dynamiccarsharing.contracts.enums.UserStatus;
import com.dynamiccarsharing.user.service.interfaces.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void registerUser_withValidData_shouldReturnCreated() throws Exception {
        ContactInfoCreateRequestDto contactInfoDto = new ContactInfoCreateRequestDto();
        contactInfoDto.setFirstName("Dumitru");
        contactInfoDto.setLastName("Diacenco");
        contactInfoDto.setEmail("dd.prodev@gmail.com");
        contactInfoDto.setPhoneNumber("+3736777388");

        UserCreateRequestDto createDto = new UserCreateRequestDto();
        createDto.setRole(UserRole.RENTER);
        createDto.setContactInfo(contactInfoDto);


        UserDto savedDto = new UserDto();
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
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_asAdmin_shouldReturnUserList() throws Exception {
        when(userService.findAllUsers()).thenReturn(List.of(new UserDto(), new UserDto()));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserStatus_asAdmin_shouldReturnOk() throws Exception {
        UserStatusUpdateRequestDto updateDto = new UserStatusUpdateRequestDto();
        updateDto.setStatus(UserStatus.SUSPENDED);

        UserDto updatedDto = new UserDto();
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
    @WithMockUser(roles = "ADMIN")
    void deleteUser_asAdmin_shouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteById(1L);

        mockMvc.perform(delete("/api/v1/users/{userId}", 1L).with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteById(1L);
    }
}
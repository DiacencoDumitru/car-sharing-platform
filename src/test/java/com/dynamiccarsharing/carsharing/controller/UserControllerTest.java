package com.dynamiccarsharing.carsharing.controller;

<<<<<<< HEAD
import com.dynamiccarsharing.carsharing.config.SecurityConfig;
import com.dynamiccarsharing.carsharing.dto.ContactInfoCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.UserCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.UserDto;
import com.dynamiccarsharing.carsharing.dto.UserStatusUpdateRequestDto;
import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.service.interfaces.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
=======
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
>>>>>>> fix/controller-mvc-tests
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
<<<<<<< HEAD

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
=======
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
>>>>>>> fix/controller-mvc-tests
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
<<<<<<< HEAD
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
=======
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
>>>>>>> fix/controller-mvc-tests

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andExpect(status().isCreated())
<<<<<<< HEAD
                .andExpect(jsonPath("$.id").value(1L));
=======
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.role").value("RENTER"))
                .andExpect(jsonPath("$.contactInfo.email").value("jane.doe@example.com"));
>>>>>>> fix/controller-mvc-tests
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_asAdmin_shouldReturnUserList() throws Exception {
<<<<<<< HEAD
        when(userService.findAllUsers()).thenReturn(List.of(new UserDto(), new UserDto()));
=======
        User user1 = User.builder().id(1L).build();
        User user2 = User.builder().id(2L).build();
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
>>>>>>> fix/controller-mvc-tests

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserStatus_asAdmin_shouldReturnOk() throws Exception {
<<<<<<< HEAD
        UserStatusUpdateRequestDto updateDto = new UserStatusUpdateRequestDto();
        updateDto.setStatus(UserStatus.SUSPENDED);

        UserDto updatedDto = new UserDto();
        updatedDto.setId(1L);
        updatedDto.setStatus(UserStatus.SUSPENDED);

        when(userService.updateUserStatus(eq(1L), any(UserStatusUpdateRequestDto.class))).thenReturn(updatedDto);
=======
        User existingUser = User.builder().id(1L).status(UserStatus.ACTIVE).build();
        User updatedUser = existingUser.withStatus(UserStatus.SUSPENDED);

        UserStatusUpdateRequestDto updateDto = new UserStatusUpdateRequestDto();
        updateDto.setStatus(UserStatus.SUSPENDED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
>>>>>>> fix/controller-mvc-tests

        mockMvc.perform(patch("/api/v1/users/{userId}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .with(csrf()))
                .andExpect(status().isOk())
<<<<<<< HEAD
=======
                .andExpect(jsonPath("$.id").value(1L))
>>>>>>> fix/controller-mvc-tests
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_asAdmin_shouldReturnNoContent() throws Exception {
<<<<<<< HEAD
        doNothing().when(userService).deleteById(1L);
=======
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        doNothing().when(userRepository).deleteById(1L);
>>>>>>> fix/controller-mvc-tests

        mockMvc.perform(delete("/api/v1/users/{userId}", 1L).with(csrf()))
                .andExpect(status().isNoContent());

<<<<<<< HEAD
        verify(userService, times(1)).deleteById(1L);
=======
        verify(userRepository, times(1)).deleteById(1L);
>>>>>>> fix/controller-mvc-tests
    }
}
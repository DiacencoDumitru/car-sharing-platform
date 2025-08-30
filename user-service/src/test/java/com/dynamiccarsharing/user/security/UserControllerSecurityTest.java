package com.dynamiccarsharing.user.security;

import com.dynamiccarsharing.contracts.dto.UserDto;
import com.dynamiccarsharing.user.controller.UserController;
import com.dynamiccarsharing.user.exception.handler.GlobalExceptionHandler;
import com.dynamiccarsharing.user.service.interfaces.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@Import({
        UserControllerSecurityTest.WebMvcTestSecurityConfig.class,
        GlobalExceptionHandler.class
})
class UserControllerSecurityTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class WebMvcTestSecurityConfig {
        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth
                            .anyRequest().permitAll())
                    .build();
        }
    }

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;

    private final String userCreateJson = """
            {
              "role": "RENTER",
              "contactInfo": {
                "firstName": "Test",
                "lastName": "User",
                "email": "test@example.com",
                "password": "password",
                "phoneNumber": "+37367773888"
              }
            }
            """;

    @Test
    @WithAnonymousUser
    void registerUser_FailsWhenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userCreateJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_RENTER")
    void registerUser_FailsForUserWithRenterRole() throws Exception {
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userCreateJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void registerUser_SucceedsForUserWithAdminRole() throws Exception {
        var dto = new UserDto();
        dto.setId(1L);
        when(userService.registerUser(any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userCreateJson)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(handler().handlerType(UserController.class))
                .andExpect(handler().methodName("registerUser"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1));
    }
}

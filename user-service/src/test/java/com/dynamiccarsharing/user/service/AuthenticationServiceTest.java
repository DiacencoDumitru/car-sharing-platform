package com.dynamiccarsharing.user.service;

import com.dynamiccarsharing.contracts.enums.UserRole;
import com.dynamiccarsharing.contracts.enums.UserStatus;
import com.dynamiccarsharing.user.controller.AuthenticationRequest;
import com.dynamiccarsharing.user.controller.AuthenticationResponse;
import com.dynamiccarsharing.user.controller.RegisterRequest;
import com.dynamiccarsharing.user.model.User;
import com.dynamiccarsharing.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void testRegister() {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("password")
                .build();

        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(jwtService.generateToken(any(User.class))).thenReturn("test.token");
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        AuthenticationResponse response = authenticationService.register(request);

        assertNotNull(response);
        assertEquals("test.token", response.getToken());

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("john@example.com", savedUser.getContactInfo().getEmail());
        assertEquals("encodedPassword", savedUser.getContactInfo().getPassword());
        assertEquals(UserRole.RENTER, savedUser.getRole());
        assertEquals(UserStatus.ACTIVE, savedUser.getStatus());
    }

    @Test
    void testAuthenticate() {
        AuthenticationRequest request = new AuthenticationRequest("user@example.com", "password");
        
        User user = User.builder().id(1L).build();
        
        when(userRepository.findByContactInfoEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("test.token");

        AuthenticationResponse response = authenticationService.authenticate(request);

        assertNotNull(response);
        assertEquals("test.token", response.getToken());

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("user@example.com", "password")
        );
    }

    @Test
    void testAuthenticate_UserNotFoundAfterAuth() {
        AuthenticationRequest request = new AuthenticationRequest("user@example.com", "password");

        when(userRepository.findByContactInfoEmail("user@example.com")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            authenticationService.authenticate(request);
        });

        assertEquals("User not found after authentication", exception.getMessage());
    }
}
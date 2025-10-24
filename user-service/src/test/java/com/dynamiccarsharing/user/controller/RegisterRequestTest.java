package com.dynamiccarsharing.user.controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RegisterRequestTest {

    @Test
    void testNoArgsConstructor() {
        RegisterRequest request = new RegisterRequest();
        assertNull(request.getEmail());
        assertNull(request.getPassword());
        assertNull(request.getFirstName());
        assertNull(request.getLastName());
    }

    @Test
    void testAllArgsConstructor() {
        RegisterRequest request = new RegisterRequest("John", "Doe", "john@example.com", "password");
        assertEquals("John", request.getFirstName());
        assertEquals("Doe", request.getLastName());
        assertEquals("john@example.com", request.getEmail());
        assertEquals("password", request.getPassword());
    }

    @Test
    void testBuilder() {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .password("builderPass")
                .build();
        
        assertEquals("Jane", request.getFirstName());
        assertEquals("Doe", request.getLastName());
        assertEquals("jane@example.com", request.getEmail());
        assertEquals("builderPass", request.getPassword());
    }

    @Test
    void testSettersAndGetters() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Setter");
        request.setLastName("User");
        request.setEmail("setter@example.com");
        request.setPassword("setterPass");

        assertEquals("Setter", request.getFirstName());
        assertEquals("User", request.getLastName());
        assertEquals("setter@example.com", request.getEmail());
        assertEquals("setterPass", request.getPassword());
    }
}
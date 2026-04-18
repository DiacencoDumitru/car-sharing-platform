package com.dynamiccarsharing.user.controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthenticationRequestTest {

    @Test
    void testNoArgsConstructor() {
        AuthenticationRequest request = new AuthenticationRequest();
        assertNull(request.getEmail());
        assertNull(request.getPassword());
    }

    @Test
    void testAllArgsConstructor() {
        AuthenticationRequest request = new AuthenticationRequest("test@example.com", "password");
        assertEquals("test@example.com", request.getEmail());
        assertEquals("password", request.getPassword());
    }

    @Test
    void testBuilder() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("builder@example.com")
                .password("builderPass")
                .build();
        assertEquals("builder@example.com", request.getEmail());
        assertEquals("builderPass", request.getPassword());
    }

    @Test
    void testSettersAndGetters() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("setter@example.com");
        request.setPassword("setterPass");

        assertEquals("setter@example.com", request.getEmail());
        assertEquals("setterPass", request.getPassword());
    }

    @Test
    void testEqualsAndHashCode() {
        AuthenticationRequest request1 = new AuthenticationRequest("test@example.com", "password");
        AuthenticationRequest request2 = new AuthenticationRequest("test@example.com", "password");
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testToString() {
        AuthenticationRequest request = new AuthenticationRequest("test@example.com", "password");
        assertTrue(request.toString().contains("test@example.com"));
    }
}
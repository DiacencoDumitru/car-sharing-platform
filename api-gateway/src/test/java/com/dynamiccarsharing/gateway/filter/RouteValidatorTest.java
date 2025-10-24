package com.dynamiccarsharing.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RouteValidatorTest {

    private RouteValidator routeValidator;
    private ServerHttpRequest request;

    @BeforeEach
    void setUp() {
        routeValidator = new RouteValidator();
        request = mock(ServerHttpRequest.class);
    }

    private void mockRequestPath(String path) throws URISyntaxException {
        when(request.getURI()).thenReturn(new URI("http://localhost" + path));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/auth/register",
            "/auth/login",
            "/api/v1/auth/register/extra",
            "/api/v1/auth/login?param=1",
            "/eureka",
            "/eureka/apps"
    })
    @DisplayName("isSecured should return false for open API endpoints")
    void isSecured_openApiEndpoints_returnsFalse(String path) throws URISyntaxException {
        mockRequestPath(path);
        assertFalse(routeValidator.isSecured.test(request));
    }
}
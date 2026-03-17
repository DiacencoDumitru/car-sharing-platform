package com.dynamiccarsharing.gateway.filter;

import com.dynamiccarsharing.util.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthenticationFilterTest {

    @Mock
    private RouteValidator validator;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private ServerWebExchange exchange;
    @Mock
    private GatewayFilterChain chain;
    @Mock
    private ServerHttpRequest request;
    @Mock
    private ServerHttpRequest.Builder requestBuilder;
    @Mock
    private ServerHttpResponse response;
    @Mock
    private HttpHeaders headers;

    @Mock
    private Predicate<ServerHttpRequest> isSecuredPredicateMock;

    @InjectMocks
    private AuthenticationFilter authenticationFilter;

    private AuthenticationFilter.Config config = new AuthenticationFilter.Config();

    @BeforeEach
    void setUp() {
        when(exchange.getRequest()).thenReturn(request);
        when(request.getURI()).thenReturn(URI.create("/api/v1/any"));
        when(request.mutate()).thenReturn(requestBuilder);
        when(requestBuilder.headers(any())).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(response.setComplete()).thenReturn(Mono.empty());
        validator.isSecured = isSecuredPredicateMock;
    }

    @Test
    @DisplayName("Should pass through if route is not secured")
    void apply_notSecuredRoute_passesThrough() {
        when(isSecuredPredicateMock.test(request)).thenReturn(false);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = authenticationFilter.apply(config).filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();

        verify(chain, times(1)).filter(exchange);
        verify(jwtUtil, never()).isTokenValid(anyString());
        verify(response, never()).setStatusCode(any());
    }

    @Test
    @DisplayName("Should return UNAUTHORIZED if secured and missing auth header")
    void apply_securedRoute_missingHeader_returnsUnauthorized() {
        when(isSecuredPredicateMock.test(request)).thenReturn(true);
        when(request.getHeaders()).thenReturn(headers);
        when(headers.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(false);

        Mono<Void> result = authenticationFilter.apply(config).filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();

        verify(response, times(1)).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(exchange);
    }

    @Test
    @DisplayName("Should return UNAUTHORIZED if secured and header exists but is empty list")
    void apply_securedRoute_emptyHeaderList_returnsUnauthorized() {
        when(isSecuredPredicateMock.test(request)).thenReturn(true);
        when(request.getHeaders()).thenReturn(headers);
        when(headers.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
        when(headers.get(HttpHeaders.AUTHORIZATION)).thenReturn(List.of());

        Mono<Void> result = authenticationFilter.apply(config).filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();

        verify(response, times(1)).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(exchange);
        verify(jwtUtil, never()).isTokenValid(any());
    }


    @Test
    @DisplayName("Should return UNAUTHORIZED if secured and invalid token")
    void apply_securedRoute_invalidToken_returnsUnauthorized() {
        String invalidToken = "invalid-token";
        String authHeader = "Bearer " + invalidToken;
        when(isSecuredPredicateMock.test(request)).thenReturn(true);
        when(request.getHeaders()).thenReturn(headers);
        when(headers.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
        when(headers.get(HttpHeaders.AUTHORIZATION)).thenReturn(List.of(authHeader));
        when(jwtUtil.isTokenValid(invalidToken)).thenReturn(false);

        Mono<Void> result = authenticationFilter.apply(config).filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();

        verify(response, times(1)).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(exchange);
        verify(jwtUtil, times(1)).isTokenValid(invalidToken);
    }

    @Test
    @DisplayName("Should pass through if secured and valid token with Bearer prefix")
    void apply_securedRoute_validTokenWithBearer_passesThrough() {
        String validToken = "valid-token";
        String authHeader = "Bearer " + validToken;
        when(isSecuredPredicateMock.test(request)).thenReturn(true);
        when(request.getURI()).thenReturn(URI.create("/api/v1/bookings"));
        when(request.getHeaders()).thenReturn(headers);
        when(headers.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
        when(headers.get(HttpHeaders.AUTHORIZATION)).thenReturn(List.of(authHeader));
        when(jwtUtil.isTokenValid(validToken)).thenReturn(true);
        when(jwtUtil.extractUserId(validToken)).thenReturn(123L);
        when(jwtUtil.extractAuthorities(validToken)).thenReturn(List.of("ROLE_USER"));
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = authenticationFilter.apply(config).filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();

        verify(jwtUtil, times(1)).isTokenValid(validToken);
    }

    @Test
    @DisplayName("Should return UNAUTHORIZED if secured and token is missing Bearer prefix")
    void apply_securedRoute_tokenMissingBearerPrefix_returnsUnauthorized() {
        String validToken = "valid-token-no-prefix";
        String authHeader = validToken;
        when(isSecuredPredicateMock.test(request)).thenReturn(true);
        when(request.getHeaders()).thenReturn(headers);
        when(headers.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
        when(headers.get(HttpHeaders.AUTHORIZATION)).thenReturn(List.of(authHeader));

        Mono<Void> result = authenticationFilter.apply(config).filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();

        verify(chain, never()).filter(exchange);
        verify(jwtUtil, never()).isTokenValid(anyString());
        verify(response, times(1)).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should handle null authHeader value gracefully")
    void apply_securedRoute_nullAuthHeaderValue_returnsUnauthorized() {
        when(isSecuredPredicateMock.test(request)).thenReturn(true);
        when(request.getHeaders()).thenReturn(headers);
        when(headers.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
        when(headers.get(HttpHeaders.AUTHORIZATION)).thenReturn(Collections.singletonList(null));

        Mono<Void> result = authenticationFilter.apply(config).filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();

        verify(response, times(1)).setStatusCode(HttpStatus.UNAUTHORIZED);

        verify(jwtUtil, never()).isTokenValid(any());
        verify(chain, never()).filter(exchange);
    }

    @Test
    @DisplayName("Should forbid admin path when user does not have ROLE_ADMIN")
    void apply_adminPath_withoutAdminRole_returnsForbidden() {
        String token = "valid-token-no-admin";
        String authHeader = "Bearer " + token;
        when(isSecuredPredicateMock.test(request)).thenReturn(true);
        when(request.getURI()).thenReturn(URI.create("/api/v1/admin/payments"));
        when(request.getHeaders()).thenReturn(headers);
        when(headers.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
        when(headers.get(HttpHeaders.AUTHORIZATION)).thenReturn(List.of(authHeader));
        when(jwtUtil.isTokenValid(token)).thenReturn(true);
        when(jwtUtil.extractUserId(token)).thenReturn(1L);
        when(jwtUtil.extractAuthorities(token)).thenReturn(List.of("ROLE_USER"));

        Mono<Void> result = authenticationFilter.apply(config).filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();

        verify(response, times(1)).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(exchange);
    }

    @Test
    @DisplayName("Should allow admin path when user has ROLE_ADMIN")
    void apply_adminPath_withAdminRole_passesThrough() {
        String token = "valid-admin-token";
        String authHeader = "Bearer " + token;
        when(isSecuredPredicateMock.test(request)).thenReturn(true);
        when(request.getURI()).thenReturn(URI.create("/api/v1/admin/payments"));
        when(request.getHeaders()).thenReturn(headers);
        when(headers.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
        when(headers.get(HttpHeaders.AUTHORIZATION)).thenReturn(List.of(authHeader));
        when(jwtUtil.isTokenValid(token)).thenReturn(true);
        when(jwtUtil.extractUserId(token)).thenReturn(42L);
        when(jwtUtil.extractAuthorities(token)).thenReturn(List.of("ROLE_ADMIN"));
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = authenticationFilter.apply(config).filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    @DisplayName("Constructor coverage for filter and config")
    void constructors() {
        assertNotNull(new AuthenticationFilter());
        assertNotNull(new AuthenticationFilter.Config());
    }
}
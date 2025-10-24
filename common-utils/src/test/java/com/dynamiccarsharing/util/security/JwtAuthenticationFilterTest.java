package com.dynamiccarsharing.util.security;

import com.dynamiccarsharing.util.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication existingAuthentication;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("doFilterInternal skips processing if Authorization header is missing")
    void doFilterInternal_noAuthHeader_skips() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtUtil, never()).isTokenValid(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal skips processing if Authorization header does not start with Bearer")
    void doFilterInternal_noBearerPrefix_skips() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic abc");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtUtil, never()).isTokenValid(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal skips processing if token is invalid (expired)")
    void doFilterInternal_expiredToken_skips() throws ServletException, IOException {
        String token = "expiredToken";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.isTokenValid(token)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtUtil, times(1)).isTokenValid(token);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal skips processing on JwtException during validation")
    void doFilterInternal_jwtException_skips() throws ServletException, IOException {
        String token = "badToken";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.isTokenValid(token)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtUtil, times(1)).isTokenValid(token);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

     @Test
    @DisplayName("doFilterInternal skips processing on IllegalArgumentException during validation")
    void doFilterInternal_illegalArgumentException_skips() throws ServletException, IOException {
        String token = "badToken";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.isTokenValid(token)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtUtil, times(1)).isTokenValid(token);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }


    @Test
    @DisplayName("doFilterInternal proceeds if authentication already exists in context")
    void doFilterInternal_authExists_skipsAuthSetting() throws ServletException, IOException {
        String token = "validToken";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.isTokenValid(token)).thenReturn(true);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(existingAuthentication);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtUtil, times(1)).isTokenValid(token);
        verify(jwtUtil, never()).extractUserId(token);
        verify(jwtUtil, never()).extractUsername(token);
        verify(jwtUtil, never()).extractAuthorities(token);
        verify(securityContext, never()).setAuthentication(any());
        assertSame(existingAuthentication, SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal sets authentication using userId if present")
    void doFilterInternal_validToken_userIdPresent_setsAuth() throws ServletException, IOException {
        String token = "validTokenWithUserId";
        Long userId = 123L;
        List<String> roles = List.of("USER");

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.isTokenValid(token)).thenReturn(true);
        when(jwtUtil.extractUserId(token)).thenReturn(userId);
        when(jwtUtil.extractAuthorities(token)).thenReturn(roles);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtUtil, times(1)).isTokenValid(token);
        verify(jwtUtil, times(1)).extractUserId(token);
        verify(jwtUtil, never()).extractUsername(token);
        verify(jwtUtil, times(1)).extractAuthorities(token);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(String.valueOf(userId), auth.getPrincipal());
        assertEquals(1, auth.getAuthorities().size());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("USER")));
    }

    @Test
    @DisplayName("doFilterInternal sets authentication using username if userId is null")
    void doFilterInternal_validToken_userIdNull_setsAuthWithUsername() throws ServletException, IOException {
        String token = "validTokenNoUserId";
        String username = "testUser";
        List<String> roles = Collections.emptyList();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.isTokenValid(token)).thenReturn(true);
        when(jwtUtil.extractUserId(token)).thenReturn(null);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(jwtUtil.extractAuthorities(token)).thenReturn(roles);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtUtil, times(1)).isTokenValid(token);
        verify(jwtUtil, times(1)).extractUserId(token);
        verify(jwtUtil, times(1)).extractUsername(token);
        verify(jwtUtil, times(1)).extractAuthorities(token);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(username, auth.getPrincipal());
        assertTrue(auth.getAuthorities().isEmpty());
    }

      @Test
    @DisplayName("doFilterInternal sets authentication with empty authorities if null returned")
    void doFilterInternal_validToken_nullAuthorities_setsAuthWithEmptyList() throws ServletException, IOException {
        String token = "validTokenNullAuths";
        Long userId = 456L;

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.isTokenValid(token)).thenReturn(true);
        when(jwtUtil.extractUserId(token)).thenReturn(userId);
        when(jwtUtil.extractAuthorities(token)).thenReturn(null);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtUtil, times(1)).isTokenValid(token);
        verify(jwtUtil, times(1)).extractUserId(token);
        verify(jwtUtil, times(1)).extractAuthorities(token);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(String.valueOf(userId), auth.getPrincipal());
        assertTrue(auth.getAuthorities().isEmpty());
    }
}
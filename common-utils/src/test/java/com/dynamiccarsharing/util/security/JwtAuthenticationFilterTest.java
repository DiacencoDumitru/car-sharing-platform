package com.dynamiccarsharing.util.security;

import com.dynamiccarsharing.util.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;


class JwtAuthenticationFilterTest {

    private final JwtUtilStub jwtUtil = new JwtUtilStub();
    private final RecordingFilterChain filterChain = new RecordingFilterChain();
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jwtUtil.reset();
        filterChain.reset();
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil);
    }

    @Test
    @DisplayName("doFilterInternal skips processing if Authorization header is missing")
    void doFilterInternal_noAuthHeader_skips() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertEquals(1, filterChain.calls.get());
        assertEquals(0, jwtUtil.isTokenValidCalls.get());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal skips processing if Authorization header does not start with Bearer")
    void doFilterInternal_noBearerPrefix_skips() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertEquals(1, filterChain.calls.get());
        assertEquals(0, jwtUtil.isTokenValidCalls.get());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal skips processing if token is invalid (expired)")
    void doFilterInternal_expiredToken_skips() throws ServletException, IOException {
        String token = "expiredToken";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtUtil.tokenValidResult = false;

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertEquals(1, filterChain.calls.get());
        assertEquals(1, jwtUtil.isTokenValidCalls.get());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal skips processing on JwtException during validation")
    void doFilterInternal_jwtException_skips() throws ServletException, IOException {
        String token = "badToken";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtUtil.tokenValidResult = false;

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertEquals(1, filterChain.calls.get());
        assertEquals(1, jwtUtil.isTokenValidCalls.get());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal skips processing on IllegalArgumentException during validation")
    void doFilterInternal_illegalArgumentException_skips() throws ServletException, IOException {
        String token = "badToken";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtUtil.tokenValidResult = false;

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertEquals(1, filterChain.calls.get());
        assertEquals(1, jwtUtil.isTokenValidCalls.get());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal proceeds if authentication already exists in context")
    void doFilterInternal_authExists_skipsAuthSetting() throws ServletException, IOException {
        String token = "validToken";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtUtil.tokenValidResult = true;

        Authentication existingAuthentication = new UsernamePasswordAuthenticationToken("existing", null, List.of());
        SecurityContextImpl ctx = new SecurityContextImpl();
        ctx.setAuthentication(existingAuthentication);
        SecurityContextHolder.setContext(ctx);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertEquals(1, filterChain.calls.get());
        assertEquals(1, jwtUtil.isTokenValidCalls.get());
        assertEquals(0, jwtUtil.extractUserIdCalls.get());
        assertEquals(0, jwtUtil.extractUsernameCalls.get());
        assertEquals(0, jwtUtil.extractAuthoritiesCalls.get());
        assertSame(existingAuthentication, SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal sets authentication using userId if present")
    void doFilterInternal_validToken_userIdPresent_setsAuth() throws ServletException, IOException {
        String token = "validTokenWithUserId";
        Long userId = 123L;
        List<String> roles = List.of("USER");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtUtil.tokenValidResult = true;
        jwtUtil.userIdToReturn = userId;
        jwtUtil.authoritiesToReturn = roles;

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertEquals(1, filterChain.calls.get());
        assertEquals(1, jwtUtil.isTokenValidCalls.get());
        assertEquals(1, jwtUtil.extractUserIdCalls.get());
        assertEquals(0, jwtUtil.extractUsernameCalls.get());
        assertEquals(1, jwtUtil.extractAuthoritiesCalls.get());

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

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtUtil.tokenValidResult = true;
        jwtUtil.userIdToReturn = null;
        jwtUtil.usernameToReturn = username;
        jwtUtil.authoritiesToReturn = roles;

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertEquals(1, filterChain.calls.get());
        assertEquals(1, jwtUtil.isTokenValidCalls.get());
        assertEquals(1, jwtUtil.extractUserIdCalls.get());
        assertEquals(1, jwtUtil.extractUsernameCalls.get());
        assertEquals(1, jwtUtil.extractAuthoritiesCalls.get());

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

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtUtil.tokenValidResult = true;
        jwtUtil.userIdToReturn = userId;
        jwtUtil.authoritiesToReturn = null;

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertEquals(1, filterChain.calls.get());
        assertEquals(1, jwtUtil.isTokenValidCalls.get());
        assertEquals(1, jwtUtil.extractUserIdCalls.get());
        assertEquals(1, jwtUtil.extractAuthoritiesCalls.get());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(String.valueOf(userId), auth.getPrincipal());
        assertTrue(auth.getAuthorities().isEmpty());
    }

    private static final class JwtUtilStub extends JwtUtil {
        final AtomicInteger isTokenValidCalls = new AtomicInteger();
        final AtomicInteger extractUserIdCalls = new AtomicInteger();
        final AtomicInteger extractUsernameCalls = new AtomicInteger();
        final AtomicInteger extractAuthoritiesCalls = new AtomicInteger();

        boolean tokenValidResult = true;
        Long userIdToReturn;
        String usernameToReturn;
        List<String> authoritiesToReturn = List.of();

        void reset() {
            isTokenValidCalls.set(0);
            extractUserIdCalls.set(0);
            extractUsernameCalls.set(0);
            extractAuthoritiesCalls.set(0);
            tokenValidResult = true;
            userIdToReturn = null;
            usernameToReturn = null;
            authoritiesToReturn = List.of();
        }

        @Override
        public boolean isTokenValid(String token) {
            isTokenValidCalls.incrementAndGet();
            return tokenValidResult;
        }

        @Override
        public Long extractUserId(String token) {
            extractUserIdCalls.incrementAndGet();
            return userIdToReturn;
        }

        @Override
        public String extractUsername(String token) {
            extractUsernameCalls.incrementAndGet();
            return usernameToReturn;
        }

        @Override
        public List<String> extractAuthorities(String token) {
            extractAuthoritiesCalls.incrementAndGet();
            return authoritiesToReturn;
        }
    }

    private static final class RecordingFilterChain implements FilterChain {
        final AtomicInteger calls = new AtomicInteger();

        void reset() {
            calls.set(0);
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) {
            calls.incrementAndGet();
        }
    }
}

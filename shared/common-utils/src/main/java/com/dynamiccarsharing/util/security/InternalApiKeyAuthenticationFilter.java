package com.dynamiccarsharing.util.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

public class InternalApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";

    private static final String INTERNAL_URI_PREFIX = "/api/v1/internal/";

    private final String configuredInternalApiKey;

    public InternalApiKeyAuthenticationFilter(String configuredInternalApiKey) {
        this.configuredInternalApiKey = configuredInternalApiKey;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (!uri.startsWith(INTERNAL_URI_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!StringUtils.hasText(configuredInternalApiKey)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Internal API is not configured");
            return;
        }

        String provided = request.getHeader(INTERNAL_API_KEY_HEADER);
        byte[] expected = configuredInternalApiKey.getBytes(StandardCharsets.UTF_8);
        byte[] actual = (provided != null ? provided : "").getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expected, actual)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid internal API key");
            return;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "internal-service",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_INTERNAL"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}

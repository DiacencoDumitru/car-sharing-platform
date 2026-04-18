package com.dynamiccarsharing.gateway.filter;

import com.dynamiccarsharing.util.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final int TOKEN_PREFIX_LENGTH = TOKEN_PREFIX.length();

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (validator.isSecured.test(request)) {
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    return this.onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
                }

                List<String> authHeaders = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
                if (authHeaders == null || authHeaders.isEmpty()) {
                    return this.onError(exchange, "Authorization header is empty", HttpStatus.UNAUTHORIZED);
                }

                String authHeader = authHeaders.get(0);

                if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
                    return this.onError(exchange, "Authorization header must be a Bearer token", HttpStatus.UNAUTHORIZED);
                }

                String token = authHeader.substring(TOKEN_PREFIX_LENGTH);

                List<String> authorities;
                try {
                    boolean valid = jwtUtil.isTokenValid(token);
                    if (!valid) {
                        log.warn("Token validation failed: token is not valid");
                        return this.onError(exchange, "Unauthorized access to application", HttpStatus.UNAUTHORIZED);
                    }

                    Long userId = jwtUtil.extractUserId(token);
                    authorities = jwtUtil.extractAuthorities(token);

                    ServerHttpRequest mutatedRequest = request.mutate()
                            .headers(headers -> {
                                if (userId != null) {
                                    headers.set("X-User-Id", userId.toString());
                                }
                                if (authorities != null && !authorities.isEmpty()) {
                                    headers.set("X-User-Roles", String.join(",", authorities));
                                }
                            })
                            .build();
                    exchange = exchange.mutate().request(mutatedRequest).build();
                    request = mutatedRequest;
                } catch (Exception e) {
                    log.warn("Token validation failed: {}", e.getMessage());
                    return this.onError(exchange, "Unauthorized access to application", HttpStatus.UNAUTHORIZED);
                }

                String path = request.getURI().getPath();
                boolean isAdminPath = path.startsWith("/api/v1/admin");
                if (isAdminPath) {
                    boolean isAdmin = authorities != null &&
                            authorities.stream().filter(Objects::nonNull).anyMatch(role -> role.equals("ROLE_ADMIN"));
                    if (!isAdmin) {
                        return this.onError(exchange, "Forbidden: admin role required", HttpStatus.FORBIDDEN);
                    }
                }
            }
            return chain.filter(exchange);
        });
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
    }
}
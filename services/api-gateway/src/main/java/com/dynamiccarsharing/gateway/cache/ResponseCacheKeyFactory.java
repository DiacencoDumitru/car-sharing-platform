package com.dynamiccarsharing.gateway.cache;

import com.dynamiccarsharing.gateway.config.ResponseCacheProperties;
import com.dynamiccarsharing.gateway.filter.RouteValidator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Component
public class ResponseCacheKeyFactory {

    private static final String CACHE_KEY_PREFIX = "gateway:resp:";
    private final ResponseCacheProperties properties;
    private final RouteValidator routeValidator;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public ResponseCacheKeyFactory(ResponseCacheProperties properties, RouteValidator routeValidator) {
        this.properties = properties;
        this.routeValidator = routeValidator;
    }

    public boolean isCacheable(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        if (path.contains("/bookings/") && path.contains("/messages")) {
            return false;
        }
        return properties.getCacheablePaths().stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    public String buildKey(ServerHttpRequest request) {
        String normalizedUri = normalizeUri(request.getURI());
        String identityPart = resolveIdentityPart(request);
        String source = request.getMethod().name() + "|" + identityPart + "|" + normalizedUri;
        return CACHE_KEY_PREFIX + sha256Hex(source);
    }

    public Set<String> resolveGroupsForPath(String path) {
        return properties.getInvalidation().entrySet().stream()
                .filter(entry -> entry.getValue().stream().anyMatch(pattern -> pathMatcher.match(pattern, path)))
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public Set<String> resolveInvalidationGroups(String path) {
        Set<String> groups = resolveGroupsForPath(path);
        if (pathMatcher.match("/api/v1/bookings/**", path)) {
            groups.add("cars");
        }
        return groups;
    }

    public String groupKey(String group) {
        return "gateway:group:" + group;
    }

    private String resolveIdentityPart(ServerHttpRequest request) {
        boolean secured = routeValidator.isSecured.test(request);
        if (!secured) {
            return "public";
        }
        List<String> authHeaders = request.getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION);
        String authToken = authHeaders.isEmpty() ? "anonymous" : authHeaders.get(0);
        return sha256Hex(authToken);
    }

    private String normalizeUri(URI uri) {
        String path = uri.getPath();
        String query = Optional.ofNullable(uri.getQuery())
                .map(q -> sortQueryParams(q.split("&")))
                .orElse("");
        return query.isEmpty() ? path : path + "?" + query;
    }

    private String sortQueryParams(String[] pairs) {
        return java.util.Arrays.stream(pairs)
                .filter(pair -> !pair.isBlank())
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.joining("&"));
    }

    private String sha256Hex(String source) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(source.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte value : hash) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Unable to hash cache key", ex);
        }
    }
}

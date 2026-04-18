package com.dynamiccarsharing.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "application.cache.response")
public class ResponseCacheProperties {

    private boolean enabled = true;
    private Duration defaultTtl = Duration.ofSeconds(30);
    private int maxBodySizeBytes = 262144;
    private List<String> cacheablePaths = List.of();
    private Map<String, List<String>> invalidation = new LinkedHashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Duration getDefaultTtl() {
        return defaultTtl;
    }

    public void setDefaultTtl(Duration defaultTtl) {
        this.defaultTtl = defaultTtl;
    }

    public int getMaxBodySizeBytes() {
        return maxBodySizeBytes;
    }

    public void setMaxBodySizeBytes(int maxBodySizeBytes) {
        this.maxBodySizeBytes = maxBodySizeBytes;
    }

    public List<String> getCacheablePaths() {
        return cacheablePaths;
    }

    public void setCacheablePaths(List<String> cacheablePaths) {
        this.cacheablePaths = cacheablePaths;
    }

    public Map<String, List<String>> getInvalidation() {
        return invalidation;
    }

    public void setInvalidation(Map<String, List<String>> invalidation) {
        this.invalidation = invalidation;
    }
}

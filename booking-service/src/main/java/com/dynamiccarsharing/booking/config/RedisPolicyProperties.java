package com.dynamiccarsharing.booking.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "application.redis")
@Data
public class RedisPolicyProperties {
    private String keyPrefix = "booking";
}

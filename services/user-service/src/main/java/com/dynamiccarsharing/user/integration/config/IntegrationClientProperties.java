package com.dynamiccarsharing.user.integration.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "application.http.clients")
public class IntegrationClientProperties {
    private long timeoutSeconds = 3;
    private long retryMaxAttempts = 2;
    private long retryBackoffMillis = 200;
}

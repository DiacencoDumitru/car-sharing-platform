package com.dynamiccarsharing.car.integration.booking;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "application.http.clients")
public class BookingIntegrationClientProperties {
    private int timeoutSeconds = 3;
    private int retryMaxAttempts = 2;
    private int retryBackoffMillis = 200;
    private String internalApiKey = "";
}

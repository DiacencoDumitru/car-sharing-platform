package com.dynamiccarsharing.booking.integration.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(IntegrationClientProperties.class)
public class IntegrationClientConfig {
}

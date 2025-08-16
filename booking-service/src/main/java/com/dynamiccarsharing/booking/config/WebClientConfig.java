package com.dynamiccarsharing.booking.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${service.user.url}")
    private String userServiceUrl;

    @Value("${service.car.url}")
    private String carServiceUrl;

    @Bean
    public WebClient userWebClient() {
        return WebClient.builder().baseUrl(userServiceUrl).build();
    }

    @Bean
    public WebClient carWebClient() {
        return WebClient.builder().baseUrl(carServiceUrl).build();
    }
}
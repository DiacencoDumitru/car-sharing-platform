package com.dynamiccarsharing.car.client;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

public class UserClientFeignConfig {

    private static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";

    @Bean
    public RequestInterceptor userServiceInternalApiKeyInterceptor(
            @Value("${USER_SERVICE_INTERNAL_API_KEY:}") String apiKey
    ) {
        return requestTemplate -> {
            if (StringUtils.hasText(apiKey)) {
                requestTemplate.header(INTERNAL_API_KEY_HEADER, apiKey);
            }
        };
    }
}

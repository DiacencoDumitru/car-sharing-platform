package com.dynamiccarsharing.user.config;

import com.dynamiccarsharing.util.security.InternalApiKeyAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InternalApiKeyFilterConfiguration {

    @Bean
    public InternalApiKeyAuthenticationFilter internalApiKeyAuthenticationFilter(
            @Value("${application.security.internal-api-key:}") String configuredInternalApiKey
    ) {
        return new InternalApiKeyAuthenticationFilter(configuredInternalApiKey);
    }

    @Bean
    public FilterRegistrationBean<InternalApiKeyAuthenticationFilter> internalApiKeyAuthenticationFilterRegistration(
            InternalApiKeyAuthenticationFilter internalApiKeyAuthenticationFilter
    ) {
        FilterRegistrationBean<InternalApiKeyAuthenticationFilter> registration =
                new FilterRegistrationBean<>(internalApiKeyAuthenticationFilter);
        registration.setEnabled(false);
        return registration;
    }
}

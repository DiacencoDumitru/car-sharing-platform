package com.dynamiccarsharing.user.security;

import org.springframework.context.ApplicationListener;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;

@Component
public class InternalApiKeyStartupValidator implements ApplicationListener<ApplicationStartedEvent> {

    private final Environment environment;

    public InternalApiKeyStartupValidator(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        if (StringUtils.hasText(environment.getProperty("application.security.internal-api-key", ""))) {
            return;
        }
        boolean testProfile = Arrays.asList(environment.getActiveProfiles()).contains("test");
        if (testProfile) {
            return;
        }
        throw new IllegalStateException(
                "application.security.internal-api-key must be set (for example via USER_SERVICE_INTERNAL_API_KEY), "
                        + "unless the Spring profile 'test' is active."
        );
    }
}

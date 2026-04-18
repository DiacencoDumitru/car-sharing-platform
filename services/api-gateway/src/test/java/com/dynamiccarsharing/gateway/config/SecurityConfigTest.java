package com.dynamiccarsharing.gateway.config;

import com.dynamiccarsharing.gateway.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("SecurityWebFilterChain bean should be created")
    void springSecurityFilterChain_beanExists() {
        SecurityWebFilterChain filterChain = context.getBean(SecurityWebFilterChain.class);
        assertNotNull(filterChain);
    }

    @Test
    @DisplayName("Constructor coverage")
    void constructor() {
        assertNotNull(new SecurityConfig());
    }
}
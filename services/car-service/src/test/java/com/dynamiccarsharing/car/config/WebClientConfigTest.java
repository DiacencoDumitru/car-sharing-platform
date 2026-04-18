package com.dynamiccarsharing.car.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.ApplicationContext;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = WebClientConfig.class)
class WebClientConfigTest {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("webClientBuilder bean exists and is LoadBalanced")
    void webClientBuilder_beanExistsAndIsLoadBalanced() throws NoSuchMethodException {
        WebClient.Builder builder = context.getBean(WebClient.Builder.class);
        assertNotNull(builder);

        Method beanMethod = WebClientConfig.class.getMethod("webClientBuilder");
        assertTrue(beanMethod.isAnnotationPresent(LoadBalanced.class));
    }

    @Test
    @DisplayName("Constructor coverage")
    void constructor() {
        assertNotNull(new WebClientConfig());
    }
}
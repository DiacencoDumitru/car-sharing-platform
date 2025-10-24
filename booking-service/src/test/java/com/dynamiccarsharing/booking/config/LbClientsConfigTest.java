package com.dynamiccarsharing.booking.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.ApplicationContext;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = LbClientsConfig.class)
class LbClientsConfigTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void loadBalancedWebClientBuilder_beanExistsAndIsLoadBalanced() throws NoSuchMethodException {
        WebClient.Builder builder = context.getBean(WebClient.Builder.class);
        assertNotNull(builder);

        Method beanMethod = LbClientsConfig.class.getMethod("loadBalancedWebClientBuilder");
        assertTrue(beanMethod.isAnnotationPresent(LoadBalanced.class));
    }

    @Test
    void constructor_coverage() {
        assertNotNull(new LbClientsConfig());
    }
}
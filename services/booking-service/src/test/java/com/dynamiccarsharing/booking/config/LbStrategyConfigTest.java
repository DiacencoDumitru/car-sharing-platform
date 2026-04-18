package com.dynamiccarsharing.booking.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.loadbalancer.core.RandomLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LbStrategyConfigTest {

    @Mock
    private Environment env;

    @Mock
    private LoadBalancerClientFactory factory;

    @Mock
    private org.springframework.beans.factory.ObjectProvider<ServiceInstanceListSupplier> provider;

    @InjectMocks
    private LbStrategyConfig lbStrategyConfig;

    @BeforeEach
    void setUp() {
        when(env.getProperty(anyString())).thenReturn("test-service");
        when(factory.getLazyProvider(eq("test-service"), eq(ServiceInstanceListSupplier.class)))
                .thenReturn(provider);
    }

    @Test
    void rr_beanCreatesRoundRobinLoadBalancer() {
        ReactorServiceInstanceLoadBalancer loadBalancer = lbStrategyConfig.rr(env, factory);
        assertNotNull(loadBalancer);
        assertTrue(loadBalancer instanceof RoundRobinLoadBalancer);
    }

    @Test
    void rnd_beanCreatesRandomLoadBalancer() {
        ReactorServiceInstanceLoadBalancer loadBalancer = lbStrategyConfig.rnd(env, factory);
        assertNotNull(loadBalancer);
        assertTrue(loadBalancer instanceof RandomLoadBalancer);
    }
}
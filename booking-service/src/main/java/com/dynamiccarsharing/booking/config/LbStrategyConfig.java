package com.dynamiccarsharing.booking.config;

/*
@Configuration
public class LbStrategyConfig {

    @Bean
    @Profile("lb-rr")
    ReactorServiceInstanceLoadBalancer rr(Environment env, LoadBalancerClientFactory factory) {
        String name = env.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new RoundRobinLoadBalancer(factory.getLazyProvider(name, ServiceInstanceListSupplier.class), name);
    }

    @Bean
    @Profile("lb-random")
    ReactorServiceInstanceLoadBalancer rnd(Environment env, LoadBalancerClientFactory factory) {
        String name = env.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new RandomLoadBalancer(factory.getLazyProvider(name, ServiceInstanceListSupplier.class), name);
    }
}
*/

package com.dynamiccarsharing.car;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "com.dynamiccarsharing.car",
        "com.dynamiccarsharing.util"
})
@EnableDiscoveryClient
@EnableFeignClients
@EnableCaching
public class CarApplication {
    public static void main(String[] args) {
        SpringApplication.run(CarApplication.class, args);
    }
}
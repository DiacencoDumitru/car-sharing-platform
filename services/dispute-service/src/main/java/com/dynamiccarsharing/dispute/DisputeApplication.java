package com.dynamiccarsharing.dispute;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.dynamiccarsharing.dispute", "com.dynamiccarsharing.util"})
@EnableDiscoveryClient
public class DisputeApplication {
    public static void main(String[] args) {
        SpringApplication.run(DisputeApplication.class, args);
    }
}
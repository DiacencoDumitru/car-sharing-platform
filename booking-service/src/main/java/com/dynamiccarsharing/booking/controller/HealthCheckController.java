package com.dynamiccarsharing.booking.controller;

import com.dynamiccarsharing.contracts.dto.CarDto;
import com.dynamiccarsharing.contracts.dto.UserDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/diagnostics")
@RequiredArgsConstructor
@Slf4j
public class HealthCheckController {

    private final WebClient.Builder webClientBuilder;

    @Value("${eureka.instance.instance-id}")
    private String instanceId;

    private WebClient userWebClient;
    private WebClient carWebClient;

    @PostConstruct
    public void init() {
        this.userWebClient = webClientBuilder.baseUrl("lb://user-service").build();
        this.carWebClient = webClientBuilder.baseUrl("lb://car-service").build();
    }

    @GetMapping("/test-load-balancing/user/{userId}")
    public ResponseEntity<Map<String, Object>> testUserServiceLoadBalancing(@PathVariable Long userId) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> response = new HashMap<>();
        response.put("bookingServiceInstance", instanceId);
        response.put("timestamp", Instant.now());
        response.put("service", "user-service");
        response.put("userId", userId);

        try {
            log.info("Testing load balancing for user-service with userId: {}", userId);
            UserDto user = userWebClient.get()
                    .uri("/api/v1/users/" + userId)
                    .retrieve()
                    .bodyToMono(UserDto.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            long responseTime = System.currentTimeMillis() - startTime;
            response.put("userFound", user != null);
            response.put("handledByInstance", user != null ? user.getInstanceId() : "unknown");
            response.put("responseTimeMs", responseTime);
            response.put("method", "load-balanced");

            log.info("Load balancing test - User {} handled by instance: {} in {}ms",
                    userId, user != null ? user.getInstanceId() : "unknown", responseTime);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Load balancing failed, falling back to direct connection test. Error: {}", e.getMessage());
            response.put("error", "Load balancing call failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/health-check")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("bookingService", "UP");
        health.put("instance", instanceId);
        health.put("timestamp", Instant.now());
        health.put("connectivity", checkConnectivity());
        return ResponseEntity.ok(health);
    }

    private Map<String, Object> checkConnectivity() {
        Map<String, Object> connectivity = new HashMap<>();
        try {
            UserDto user = userWebClient.get()
                    .uri("/api/v1/users/1")
                    .retrieve()
                    .bodyToMono(UserDto.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();
            connectivity.put("user-service", "UP - Responded from instance: " + (user != null ? user.getInstanceId() : "unknown"));
        } catch (Exception e) {
            connectivity.put("user-service", "DOWN - Error: " + e.getMessage());
        }

        try {
            CarDto car = carWebClient.get()
                    .uri("/1")
                    .retrieve()
                    .bodyToMono(CarDto.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();
            connectivity.put("car-service", "UP - Responded from instance: " + (car != null ? car.getInstanceId() : "unknown"));
        } catch (Exception e) {
            connectivity.put("car-service", "DOWN - Error: " + e.getMessage());
        }
        return connectivity;
    }
}
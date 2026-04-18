package com.dynamiccarsharing.car.web;

import com.dynamiccarsharing.car.search.es.CarSearchService;
import com.dynamiccarsharing.car.search.es.DemoSwitch;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/cb-demo")
@RequiredArgsConstructor
public class CircuitBreakerDemoController {

    private final DemoSwitch demoSwitch;
    private final CarSearchService carSearchService;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    @PostMapping("/mode")
    public ResponseEntity<String> setMode(@RequestParam("mode") String mode) {
        DemoSwitch.Mode m = switch (mode.toLowerCase()) {
            case "ok" -> DemoSwitch.Mode.OK;
            case "fail" -> DemoSwitch.Mode.FAIL;
            case "slow" -> DemoSwitch.Mode.SLOW;
            default -> DemoSwitch.Mode.OK;
        };
        demoSwitch.set(m);
        return ResponseEntity.ok("Demo mode set to: " + m);
    }

    @PostMapping("/index/{carId}")
    public ResponseEntity<String> index(@PathVariable Long carId) {
        CircuitBreaker cb = circuitBreakerFactory.create("carSearch");
        cb.run(() -> {
            carSearchService.indexCar(carId);
            return null;
        }, ex -> null);
        return ResponseEntity.ok("Triggered index for car " + carId + " via CB 'carSearch'");
    }

    @DeleteMapping("/index/{carId}")
    public ResponseEntity<String> delete(@PathVariable Long carId) {
        CircuitBreaker cb = circuitBreakerFactory.create("carSearch");
        cb.run(() -> {
            carSearchService.deleteFromIndex(carId);
            return null;
        }, ex -> null);
        return ResponseEntity.ok("Triggered delete for car " + carId + " via CB 'carSearch'");
    }
}

package com.dynamiccarsharing.car.web;

import com.dynamiccarsharing.car.search.es.CarSearchService;
import com.dynamiccarsharing.car.search.es.DemoSwitch;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/demo")
@RequiredArgsConstructor
public class ResilienceDemoController {

    private final DemoSwitch demoSwitch;
    private final CarSearchService carSearchService;
    private final CircuitBreakerFactory<?, ?> cbFactory;

    @PostMapping("/mode/{mode}")
    public ResponseEntity<String> setMode(@PathVariable String mode) {
        DemoSwitch.Mode m;
        try {
            m = DemoSwitch.Mode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Use one of: OK, FAIL, SLOW");
        }
        demoSwitch.set(m);
        return ResponseEntity.ok("Demo mode set to: " + m);
    }

    @PostMapping("/index/{carId}")
    public ResponseEntity<String> indexViaCB(@PathVariable Long carId) {
        CircuitBreaker cb = cbFactory.create("carSearch");
        cb.run(() -> {
                    carSearchService.indexCar(carId);
                    return null;
                },
                ex -> null
        );
        return ResponseEntity.accepted().body("Triggered indexing for carId=" + carId + " via CB 'carSearch'");
    }
}

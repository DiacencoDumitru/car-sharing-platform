package com.dynamiccarsharing.car.search.es;

import com.dynamiccarsharing.car.criteria.CarSearchCriteria;
import com.dynamiccarsharing.contracts.dto.CarDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/cars")
@RequiredArgsConstructor
public class CarSearchController {

    private final CarSearchService carSearchService;

    @Value("${eureka.instance.instance-id:car-service:unknown}")
    String instanceId;

    @GetMapping("/search")
    public Page<CarDto> search(
            @RequestParam(value = "q", required = false) String q,
            CarSearchCriteria criteria,
            Pageable pageable
    ) {
        Page<CarDto> page = carSearchService.search(q, criteria, pageable);
        page.forEach(dto -> dto.setInstanceId(instanceId));
        return page;
    }

    @GetMapping("/{carId}/similar")
    public Page<CarDto> similar(@PathVariable Long carId, Pageable pageable) {
        Page<CarDto> page = carSearchService.findSimilar(carId, pageable);
        page.forEach(dto -> dto.setInstanceId(instanceId));
        return page;
    }
}

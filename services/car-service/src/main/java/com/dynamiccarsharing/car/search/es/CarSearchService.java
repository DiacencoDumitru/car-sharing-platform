package com.dynamiccarsharing.car.search.es;

import com.dynamiccarsharing.car.criteria.CarSearchCriteria;
import com.dynamiccarsharing.contracts.dto.CarDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarSearchService {

    Page<CarDto> search(String q, CarSearchCriteria criteria, Pageable pageable);

    Page<CarDto> findSimilar(Long carId, Pageable pageable);

    void indexCar(Long carId);

    void deleteFromIndex(Long carId);

}

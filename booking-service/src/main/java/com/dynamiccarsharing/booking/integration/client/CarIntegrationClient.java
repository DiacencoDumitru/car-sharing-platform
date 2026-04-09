package com.dynamiccarsharing.booking.integration.client;

import com.dynamiccarsharing.contracts.dto.CarDto;

public interface CarIntegrationClient {
    void assertCarAvailable(Long carId);

    CarDto getCarById(Long carId);
}

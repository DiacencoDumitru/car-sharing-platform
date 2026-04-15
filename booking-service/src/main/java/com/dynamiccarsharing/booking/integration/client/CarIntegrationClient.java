package com.dynamiccarsharing.booking.integration.client;

import com.dynamiccarsharing.contracts.dto.CarDto;

import java.util.List;

public interface CarIntegrationClient {
    void assertCarAvailable(Long carId);

    CarDto getCarById(Long carId);

    List<Long> findCarIdsByOwner(Long ownerId);
}

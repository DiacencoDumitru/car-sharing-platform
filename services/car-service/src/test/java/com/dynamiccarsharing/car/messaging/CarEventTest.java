package com.dynamiccarsharing.car.messaging;

import com.dynamiccarsharing.car.messaging.dto.CarEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class CarEventTest {

    @Test
    void testLombokAnnotations() {
        String type = "CAR_CREATED";
        Long carId = 1L;
        Instant occurredAt = Instant.now();
        Long ownerId = 10L;
        Long locationId = 100L;
        String make = "Toyota";
        String model = "Camry";
        String status = "AVAILABLE";
        String verificationStatus = "VERIFIED";
        String typeEnum = "SEDAN";
        Double pricePerDay = 50.0;

        CarEvent event1 = CarEvent.builder()
                .type(type)
                .carId(carId)
                .occurredAt(occurredAt)
                .ownerId(ownerId)
                .locationId(locationId)
                .make(make)
                .model(model)
                .status(status)
                .verificationStatus(verificationStatus)
                .typeEnum(typeEnum)
                .pricePerDay(pricePerDay)
                .build();

        CarEvent event2 = new CarEvent(type, carId, occurredAt, ownerId, locationId, make, model, status, verificationStatus, typeEnum, pricePerDay);

        CarEvent event3 = new CarEvent();
        event3.setType(type);
        event3.setCarId(carId);
        event3.setOccurredAt(occurredAt);
        event3.setOwnerId(ownerId);
        event3.setLocationId(locationId);
        event3.setMake(make);
        event3.setModel(model);
        event3.setStatus(status);
        event3.setVerificationStatus(verificationStatus);
        event3.setTypeEnum(typeEnum);
        event3.setPricePerDay(pricePerDay);

        assertEquals(type, event1.getType());
        assertEquals(carId, event1.getCarId());
        assertEquals(occurredAt, event1.getOccurredAt());
        assertEquals(ownerId, event1.getOwnerId());
        assertEquals(locationId, event1.getLocationId());
        assertEquals(make, event1.getMake());
        assertEquals(model, event1.getModel());
        assertEquals(status, event1.getStatus());
        assertEquals(verificationStatus, event1.getVerificationStatus());
        assertEquals(typeEnum, event1.getTypeEnum());
        assertEquals(pricePerDay, event1.getPricePerDay());

        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
        assertEquals(event1, event3);
        assertEquals(event1.hashCode(), event3.hashCode());

        CarEvent event4 = CarEvent.builder().carId(99L).build();
        assertNotEquals(event1, event4);
    }
}
package com.dynamiccarsharing.car.search.es;

import com.dynamiccarsharing.contracts.enums.CarStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CarDocumentTest {

    @Test
    @DisplayName("Should test Builder, Getters, and Setters")
    void testBuilderAndAccessors() {
        CarDocument doc = CarDocument.builder()
                .id("1")
                .make("Tesla")
                .model("Model 3")
                .status(CarStatus.AVAILABLE)
                .build();

        assertEquals("1", doc.getId());
        assertEquals("Tesla", doc.getMake());
        assertEquals("Model 3", doc.getModel());
        assertEquals(CarStatus.AVAILABLE, doc.getStatus());

        doc.setMake("Ford");
        doc.setModel("Mustang");
        assertEquals("Ford", doc.getMake());
        assertEquals("Mustang", doc.getModel());
    }

    @Test
    @DisplayName("Should test No-Args Constructor")
    void testNoArgsConstructor() {
        CarDocument doc = new CarDocument();
        assertNotNull(doc);
        doc.setId("2");
        assertEquals("2", doc.getId());
    }

    @Test
    @DisplayName("Should test All-Args Constructor")
    void testAllArgsConstructor() {
        CarDocument doc = new CarDocument(
                "1", "Honda", "Civic", CarStatus.MAINTENANCE, null,
                null, 120.0, "ABC-456", 1L, "City", "State", "Zip", 2L,
                4.5, 3
        );
        
        assertEquals("1", doc.getId());
        assertEquals("Honda", doc.getMake());
        assertEquals("Civic", doc.getModel());
        assertEquals(CarStatus.MAINTENANCE, doc.getStatus());
        assertEquals(120.0, doc.getPricePerDay());
        assertEquals("ABC-456", doc.getRegistrationNumber());
        assertEquals(1L, doc.getLocationId());
        assertEquals("City", doc.getLocationCity());
        assertEquals("State", doc.getLocationState());
        assertEquals("Zip", doc.getLocationZip());
        assertEquals(2L, doc.getOwnerId());
    }
}
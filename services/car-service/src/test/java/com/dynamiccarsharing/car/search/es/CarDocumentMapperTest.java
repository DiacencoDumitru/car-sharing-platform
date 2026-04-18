package com.dynamiccarsharing.car.search.es;

import com.dynamiccarsharing.car.model.Car;
import com.dynamiccarsharing.car.model.Location;
import com.dynamiccarsharing.contracts.dto.CarDto;
import com.dynamiccarsharing.contracts.enums.CarStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CarDocumentMapperTest {

    private CarDocumentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(CarDocumentMapper.class);
    }

    @Test
    @DisplayName("Should map Car to CarDocument correctly")
    void testToDocument() {
        Location location = Location.builder()
                .id(10L)
                .city("New York")
                .state("NY")
                .zipCode("10001")
                .build();

        Car car = Car.builder()
                .id(1L)
                .make("Toyota")
                .model("Camry")
                .status(CarStatus.AVAILABLE)
                .price(new BigDecimal("100.50"))
                .registrationNumber("XYZ-123")
                .location(location)
                .ownerId(5L)
                .build();

        CarDocument doc = mapper.toDocument(car);

        assertNotNull(doc);
        assertEquals("1", doc.getId());
        assertEquals("Toyota", doc.getMake());
        assertEquals("Camry", doc.getModel());
        assertEquals(CarStatus.AVAILABLE, doc.getStatus());
        assertEquals(100.50, doc.getPricePerDay());
        assertEquals("XYZ-123", doc.getRegistrationNumber());
        assertEquals(10L, doc.getLocationId());
        assertEquals("New York", doc.getLocationCity());
        assertEquals("NY", doc.getLocationState());
        assertEquals("10001", doc.getLocationZip());
        assertEquals(5L, doc.getOwnerId());
    }

    @Test
    @DisplayName("Should map CarDocument to CarDto correctly")
    void testToDto() {
        CarDocument doc = CarDocument.builder()
                .id("1")
                .make("Toyota")
                .model("Camry")
                .status(CarStatus.AVAILABLE)
                .pricePerDay(100.50)
                .registrationNumber("XYZ-123")
                .locationId(10L)
                .locationCity("New York")
                .locationState("NY")
                .locationZip("10001")
                .ownerId(5L)
                .build();

        CarDto dto = mapper.toDto(doc);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Toyota", dto.getMake());
        assertEquals("Camry", dto.getModel());
        assertEquals(CarStatus.AVAILABLE, dto.getStatus());
        assertEquals(0, new BigDecimal("100.50").compareTo(dto.getPrice()));
        assertEquals("XYZ-123", dto.getRegistrationNumber());
        assertEquals(10L, dto.getLocationId());
        assertEquals(5L, dto.getOwnerId());
    }

    @Test
    @DisplayName("Should handle nulls in helper methods")
    void testHelperMethodNulls() {
        assertNull(mapper.toDouble(null));
        assertNull(mapper.toBigDecimal(null));
    }

    @Test
    @DisplayName("Should handle nulls during mapping")
    void testMappingNulls() {
        assertNull(mapper.toDocument(null));

        assertNull(mapper.toDto(null));

        Car carWithNullPrice = Car.builder().id(1L).location(Location.builder().build()).build();
        CarDocument doc = mapper.toDocument(carWithNullPrice);
        assertNull(doc.getPricePerDay());

        CarDocument docWithNullPrice = new CarDocument();
        CarDto dto = mapper.toDto(docWithNullPrice);
        assertNull(dto.getPrice());
    }
}
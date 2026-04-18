package com.dynamiccarsharing.booking.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingCreateRequestDtoTest {

    private final LocalDateTime start = LocalDateTime.now().plusDays(1);
    private final LocalDateTime end = LocalDateTime.now().plusDays(2);

    private BookingCreateRequestDto createDto() {
        BookingCreateRequestDto dto = new BookingCreateRequestDto();
        dto.setRenterId(1L);
        dto.setCarId(2L);
        dto.setStartTime(start);
        dto.setEndTime(end);
        dto.setPickupLocationId(3L);
        return dto;
    }

    @Test
    void testGettersAndSetters() {
        BookingCreateRequestDto dto = new BookingCreateRequestDto();
        assertNull(dto.getCarId());
        
        dto.setRenterId(10L);
        dto.setCarId(20L);
        dto.setStartTime(start);
        dto.setEndTime(end);
        dto.setPickupLocationId(30L);

        assertEquals(10L, dto.getRenterId());
        assertEquals(20L, dto.getCarId());
        assertEquals(start, dto.getStartTime());
        assertEquals(end, dto.getEndTime());
        assertEquals(30L, dto.getPickupLocationId());
    }

    @Test
    void testToString() {
        BookingCreateRequestDto dto = createDto();
        String s = dto.toString();
        
        assertTrue(s.contains("renterId=1"));
        assertTrue(s.contains("carId=2"));
        assertTrue(s.contains("pickupLocationId=3"));
    }

    @Test
    void testEqualsAndHashCode_BranchCoverage() {
        BookingCreateRequestDto dto1 = createDto();
        BookingCreateRequestDto dto2 = createDto();

        assertEquals(dto1, dto1);
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, null);
        assertNotEquals(dto1, new Object());

        BookingCreateRequestDto dto3;

        dto3 = createDto();
        dto3.setRenterId(99L);
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());

        dto3 = createDto();
        dto3.setCarId(99L);
        assertNotEquals(dto1, dto3);

        dto3 = createDto();
        dto3.setStartTime(start.plusDays(1));
        assertNotEquals(dto1, dto3);
        
        dto3 = createDto();
        dto3.setEndTime(end.plusDays(1));
        assertNotEquals(dto1, dto3);
        
        dto3 = createDto();
        dto3.setPickupLocationId(99L);
        assertNotEquals(dto1, dto3);
    }
}
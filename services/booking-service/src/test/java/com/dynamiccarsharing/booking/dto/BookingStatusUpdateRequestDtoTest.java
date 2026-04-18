package com.dynamiccarsharing.booking.dto;

import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookingStatusUpdateRequestDtoTest {

    private BookingStatusUpdateRequestDto createDto() {
        BookingStatusUpdateRequestDto dto = new BookingStatusUpdateRequestDto();
        dto.setStatus(TransactionStatus.COMPLETED);
        return dto;
    }

    @Test
    void testGettersAndSetters() {
        BookingStatusUpdateRequestDto dto = new BookingStatusUpdateRequestDto();
        assertNull(dto.getStatus());
        
        dto.setStatus(TransactionStatus.PENDING);
        assertEquals(TransactionStatus.PENDING, dto.getStatus());
    }

    @Test
    void testToString() {
        BookingStatusUpdateRequestDto dto = createDto();
        assertTrue(dto.toString().contains("status=COMPLETED"));
    }

    @Test
    void testEqualsAndHashCode_BranchCoverage() {
        BookingStatusUpdateRequestDto dto1 = createDto();
        BookingStatusUpdateRequestDto dto2 = createDto();
        
        assertEquals(dto1, dto1);
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        
        assertNotEquals(dto1, null);
        assertNotEquals(dto1, new Object());

        BookingStatusUpdateRequestDto dto3 = new BookingStatusUpdateRequestDto();
        dto3.setStatus(TransactionStatus.PENDING);
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }
}
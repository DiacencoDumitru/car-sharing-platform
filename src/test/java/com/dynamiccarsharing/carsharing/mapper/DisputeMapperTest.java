package com.dynamiccarsharing.carsharing.mapper;

import com.dynamiccarsharing.carsharing.dto.DisputeCreateRequestDto;
import com.dynamiccarsharing.carsharing.model.Dispute;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class DisputeMapperTest {

    @Autowired
    private DisputeMapper disputeMapper;

    @Test
    void toEntity_shouldCorrectlyMapIdsToNestedObjects() {
        DisputeCreateRequestDto dto = new DisputeCreateRequestDto();
        dto.setDescription("Test dispute");

        Long bookingId = 30L;
        Long creationUserId = 40L;

        Dispute result = disputeMapper.toEntity(dto, bookingId, creationUserId);

        assertNotNull(result);
        assertEquals("Test dispute", result.getDescription());

        assertNotNull(result.getBooking());
        assertEquals(bookingId, result.getBooking().getId());

        assertNotNull(result.getCreationUser());
        assertEquals(creationUserId, result.getCreationUser().getId());
    }
}
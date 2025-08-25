package com.dynamiccarsharing.dispute.mapper;

import com.dynamiccarsharing.dispute.dto.DisputeCreateRequestDto;
import com.dynamiccarsharing.dispute.model.Dispute;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DisputeMapperTest {

    private final DisputeMapper disputeMapper = Mappers.getMapper(DisputeMapper.class);

    @Test
    void toEntity_shouldCorrectlyMapIdsToNestedObjects() {
        DisputeCreateRequestDto dto = new DisputeCreateRequestDto();
        dto.setDescription("Test dispute");

        Long bookingId = 30L;
        Long creationUserId = 40L;

        Dispute result = disputeMapper.toEntity(dto, bookingId, creationUserId);

        assertNotNull(result);
        assertEquals("Test dispute", result.getDescription());
        assertEquals(bookingId, result.getBookingId());
        assertEquals(creationUserId, result.getCreationUserId());
        assertNotNull(result.getCreatedAt());
    }
}
package com.dynamiccarsharing.carsharing.mapper;

import com.dynamiccarsharing.carsharing.dto.PaymentRequestDto;
import com.dynamiccarsharing.carsharing.model.Payment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class PaymentMapperTest {

    @Autowired
    private PaymentMapper paymentMapper;

    @Test
    void toEntity_shouldCorrectlyMapBookingIdToNestedBooking() {
        PaymentRequestDto dto = new PaymentRequestDto();
        dto.setAmount(new BigDecimal("100.00"));

        Long bookingId = 50L;

        Payment result = paymentMapper.toEntity(dto, bookingId);

        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result.getAmount());

        assertNotNull(result.getBooking());
        assertEquals(bookingId, result.getBooking().getId());
    }
}
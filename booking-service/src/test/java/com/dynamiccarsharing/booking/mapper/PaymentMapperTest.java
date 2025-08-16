package com.dynamiccarsharing.booking.mapper;

import com.dynamiccarsharing.contracts.dto.PaymentRequestDto;
import com.dynamiccarsharing.contracts.enums.PaymentType;
import com.dynamiccarsharing.booking.model.Payment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PaymentMapperImpl.class, BookingMapperImpl.class})
class PaymentMapperTest {

    @Autowired
    private PaymentMapper paymentMapper;

    @Test
    void toEntity_shouldCorrectlyMapBookingIdToNestedBooking() {
        PaymentRequestDto dto = new PaymentRequestDto();
        dto.setAmount(new BigDecimal("100.00"));
        dto.setPaymentMethod(PaymentType.PAYPAL);

        Long bookingId = 50L;
        dto.setBookingId(bookingId);

        Payment result = paymentMapper.toEntity(dto);

        assertNotNull(result);
        assertEquals(0, new BigDecimal("100.00").compareTo(result.getAmount()));

        assertNotNull(result.getBooking());
        assertEquals(bookingId, result.getBooking().getId());
    }
}
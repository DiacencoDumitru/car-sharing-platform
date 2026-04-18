package com.dynamiccarsharing.booking.dto;

import com.dynamiccarsharing.contracts.enums.PaymentType;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaymentRequestDtoTest {

    private PaymentRequestDto createDto() {
        PaymentRequestDto dto = new PaymentRequestDto();
        dto.setBookingId(10L);
        dto.setAmount(new BigDecimal("150.00"));
        dto.setPaymentMethod(PaymentType.CREDIT_CARD);
        return dto;
    }

    @Test
    void testGettersAndSetters() {
        PaymentRequestDto dto = new PaymentRequestDto();
        assertNull(dto.getBookingId());

        dto.setBookingId(1L);
        dto.setAmount(BigDecimal.TEN);
        dto.setPaymentMethod(PaymentType.DEBIT_CARD);

        assertEquals(1L, dto.getBookingId());
        assertEquals(BigDecimal.TEN, dto.getAmount());
        assertEquals(PaymentType.DEBIT_CARD, dto.getPaymentMethod());
    }

    @Test
    void testToString() {
        PaymentRequestDto dto = createDto();
        String s = dto.toString();
        assertTrue(s.contains("bookingId=10"));
        assertTrue(s.contains("amount=150.00"));
        assertTrue(s.contains("paymentMethod=CREDIT_CARD"));
    }

    @Test
    void testEqualsAndHashCode_BranchCoverage() {
        PaymentRequestDto dto1 = createDto();
        PaymentRequestDto dto2 = createDto();

        assertEquals(dto1, dto1);
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, null);
        assertNotEquals(dto1, new Object());

        PaymentRequestDto dto3;

        dto3 = createDto();
        dto3.setBookingId(20L);
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());

        dto3 = createDto();
        dto3.setAmount(new BigDecimal("99.00"));
        assertNotEquals(dto1, dto3);
        
        dto3 = createDto();
        dto3.setPaymentMethod(PaymentType.DEBIT_CARD);
        assertNotEquals(dto1, dto3);
    }
}
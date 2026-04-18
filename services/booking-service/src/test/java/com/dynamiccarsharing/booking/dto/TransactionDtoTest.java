package com.dynamiccarsharing.booking.dto;

import com.dynamiccarsharing.contracts.enums.PaymentType;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransactionDtoTest {

    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDateTime later = now.plusHours(1);

    private TransactionDto createDto() {
        TransactionDto dto = new TransactionDto();
        dto.setId(1L);
        dto.setBookingId(10L);
        dto.setAmount(new BigDecimal("100.00"));
        dto.setStatus(TransactionStatus.COMPLETED); 
        dto.setPaymentMethod(PaymentType.CREDIT_CARD);
        dto.setCreatedAt(now);
        dto.setUpdatedAt(later);
        return dto;
    }

    @Test
    void testGettersAndSetters() {
        TransactionDto dto = createDto();

        assertEquals(1L, dto.getId());
        assertEquals(10L, dto.getBookingId());
        assertEquals(new BigDecimal("100.00"), dto.getAmount());
        assertEquals(TransactionStatus.COMPLETED, dto.getStatus());
        assertEquals(PaymentType.CREDIT_CARD, dto.getPaymentMethod());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(later, dto.getUpdatedAt());
    }

    @Test
    void testToString() {
        TransactionDto dto = createDto();
        String s = dto.toString();

        assertTrue(s.contains("id=1"));
        assertTrue(s.contains("bookingId=10"));
        assertTrue(s.contains("amount=100.00"));
        assertTrue(s.contains("status=COMPLETED"));
    }

    @Test
    void testEqualsAndHashCode_BranchCoverage() {
        TransactionDto dto1 = createDto();
        TransactionDto dto2 = createDto();

        assertEquals(dto1, dto1);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());

        assertNotEquals(dto1, null);

        assertNotEquals(dto1, new Object());

        TransactionDto dto3;

        dto3 = createDto();
        dto3.setId(2L);
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());

        dto3 = createDto();
        dto3.setBookingId(20L);
        assertNotEquals(dto1, dto3);

        dto3 = createDto();
        dto3.setAmount(new BigDecimal("99.00"));
        assertNotEquals(dto1, dto3);

        dto3 = createDto();
        dto3.setStatus(TransactionStatus.PENDING);
        assertNotEquals(dto1, dto3);

        dto3 = createDto();
        dto3.setPaymentMethod(PaymentType.DEBIT_CARD);
        assertNotEquals(dto1, dto3);

        dto3 = createDto();
        dto3.setCreatedAt(now.minusDays(1));
        assertNotEquals(dto1, dto3);

        dto3 = createDto();
        dto3.setUpdatedAt(later.plusDays(1_000));
        assertNotEquals(dto1, dto3);
    }
}
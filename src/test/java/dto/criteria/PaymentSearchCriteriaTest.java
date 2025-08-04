package dto.criteria;

import com.dynamiccarsharing.carsharing.dto.criteria.PaymentSearchCriteria;
import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PaymentSearchCriteriaTest {

    @Test
    void builder_withAllFields_setsAndGetsAllFields() {
        Long bookingId = 1L;
        BigDecimal amount = new BigDecimal("99.99");
        TransactionStatus status = TransactionStatus.COMPLETED;
        PaymentType paymentMethod = PaymentType.CREDIT_CARD;

        PaymentSearchCriteria criteria = PaymentSearchCriteria.builder()
                .bookingId(bookingId)
                .amount(amount)
                .status(status)
                .paymentMethod(paymentMethod)
                .build();

        assertNotNull(criteria);
        assertEquals(bookingId, criteria.getBookingId());
        assertEquals(amount, criteria.getAmount());
        assertEquals(status, criteria.getStatus());
        assertEquals(paymentMethod, criteria.getPaymentMethod());
    }
}
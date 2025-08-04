package dto.criteria;

import com.dynamiccarsharing.carsharing.dto.criteria.TransactionSearchCriteria;
import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TransactionSearchCriteriaTest {

    @Test
    void builder_withAllFields_setsAndGetsAllFields() {
        Long bookingId = 1L;
        TransactionStatus status = TransactionStatus.PENDING;
        PaymentType paymentMethod = PaymentType.PAYPAL;

        TransactionSearchCriteria criteria = TransactionSearchCriteria.builder()
                .bookingId(bookingId)
                .status(status)
                .paymentMethod(paymentMethod)
                .build();

        assertNotNull(criteria);
        assertEquals(bookingId, criteria.getBookingId());
        assertEquals(status, criteria.getStatus());
        assertEquals(paymentMethod, criteria.getPaymentMethod());
    }
}
package com.dynamiccarsharing.booking.criteria;

import com.dynamiccarsharing.contracts.enums.PaymentType;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSearchCriteria {
    private Long bookingId;
    private BigDecimal amount;
    private TransactionStatus status;
    private PaymentType paymentMethod;

    public boolean hasAnyFilter() {
        return bookingId != null
                || amount != null
                || status != null
                || paymentMethod != null;
    }
}
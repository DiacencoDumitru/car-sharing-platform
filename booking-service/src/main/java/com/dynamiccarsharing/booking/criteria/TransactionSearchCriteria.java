package com.dynamiccarsharing.booking.criteria;

import com.dynamiccarsharing.contracts.enums.PaymentType;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSearchCriteria {
    private Long bookingId;
    private TransactionStatus status;
    private PaymentType paymentMethod;

    public boolean hasAnyFilter() {
        return bookingId != null
                || status != null
                || paymentMethod != null;
    }
}
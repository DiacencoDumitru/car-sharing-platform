package com.dynamiccarsharing.booking.service;

import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.model.Payment;
import com.dynamiccarsharing.contracts.enums.PaymentType;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PaymentCreationFactory {

    public Payment buildPendingPayment(Booking booking, BigDecimal amount, PaymentType paymentMethod) {
        return Payment.builder()
                .booking(booking)
                .amount(amount)
                .paymentMethod(paymentMethod)
                .status(TransactionStatus.PENDING)
                .build();
    }
}

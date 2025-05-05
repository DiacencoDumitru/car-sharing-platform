package com.dynamiccarsharing.carsharing.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class Payment {
    private Long id;
    private Long bookingId;
    private double amount;
    private String status; // will be something like: "pending", "approved", "completed"
    private String paymentMethod;
    private String transactionId;
}

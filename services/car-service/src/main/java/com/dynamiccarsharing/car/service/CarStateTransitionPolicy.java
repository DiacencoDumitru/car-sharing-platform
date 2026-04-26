package com.dynamiccarsharing.car.service;

import com.dynamiccarsharing.car.exception.InvalidCarStatusException;
import com.dynamiccarsharing.car.exception.InvalidVerificationStatusException;
import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.contracts.enums.VerificationStatus;
import org.springframework.stereotype.Component;

@Component
public class CarStateTransitionPolicy {

    public void ensureCurrentStatus(CarStatus currentStatus, CarStatus expectedStatus, String message) {
        if (currentStatus != expectedStatus) {
            throw new InvalidCarStatusException(message);
        }
    }

    public void ensurePendingVerification(VerificationStatus currentStatus, String message) {
        if (currentStatus != VerificationStatus.PENDING) {
            throw new InvalidVerificationStatusException(message);
        }
    }
}

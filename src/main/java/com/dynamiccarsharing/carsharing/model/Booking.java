package com.dynamiccarsharing.carsharing.model;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.util.Validator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.With;

import java.time.LocalDateTime;

@Getter
@ToString
@EqualsAndHashCode
public class Booking {
    private final Long id;
    private final Long renterId;
    private final Long carId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    @With
    private final TransactionStatus status;
    private final Location pickupLocation;
    @With
    private final String disputeDescription;
    @With
    private final DisputeStatus disputeStatus;

    public Booking(Long id, Long renterId, Long carId, LocalDateTime startTime, LocalDateTime endTime, TransactionStatus status, Location pickupLocation, String disputeDescription, DisputeStatus disputeStatus) {
        Validator.validateId(id, "ID");
        Validator.validateId(renterId, "Renter ID");
        Validator.validateId(carId, "Car ID");
        Validator.validateDates(startTime, endTime, "Start time", "End time");
        Validator.validateNonNull(pickupLocation, "Pickup location");
        this.id = id;
        this.renterId = renterId;
        this.carId = carId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.pickupLocation = pickupLocation;
        this.disputeDescription = disputeDescription;
        this.disputeStatus = disputeStatus;
    }
}

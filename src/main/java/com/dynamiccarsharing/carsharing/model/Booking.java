package com.dynamiccarsharing.carsharing.model;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.util.Validator;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@ToString
@Getter
public class Booking {
    private final Long id;
    private final Long renterId;
    private final Long carId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final TransactionStatus status; // will be something like: "pending", "approved", "completed"
    private final Location pickupLocation;
    private final String disputeDescription; // for admin
    private final DisputeStatus disputeStatus; // "open", "resolved"

    public Booking(Long id, Long renterId, Long carId, LocalDateTime startTime, LocalDateTime endTime, TransactionStatus status, Location pickupLocation, String disputeDescription, DisputeStatus disputeStatus) {
        Validator.validateId(id, "ID");
        Validator.validateId(renterId, "Renter ID");
        Validator.validateId(carId, "Car ID");
        Validator.validateDates(startTime, endTime, "Start time", "End time");
        Validator.validateNonNull(pickupLocation, "Pickup location");
//        Validator.validateOptionalString(disputeDescription, "Dispute description");
//        Validator.validateNonNull(disputeStatus, "Dispute status");
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

    public Booking withStatus(TransactionStatus status) {
        return new Booking(this.id, this.renterId, this.carId, this.startTime, this.endTime, status, this.pickupLocation, this.disputeDescription, this.disputeStatus);
    }

    public Booking withDispute(String disputeDescription, DisputeStatus disputeStatus) {
        return new Booking(this.id, this.renterId, this.carId, this.startTime, this.endTime, this.status, this.pickupLocation, disputeDescription, disputeStatus);
    }
}

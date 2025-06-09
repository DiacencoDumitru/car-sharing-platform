package com.dynamiccarsharing.carsharing.model;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.util.Validator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.With;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @With
    private final List<Transaction> transactions;

    public Booking(Long id, Long renterId, Long carId, LocalDateTime startTime, LocalDateTime endTime, TransactionStatus status, Location pickupLocation, String disputeDescription, DisputeStatus disputeStatus, List<Transaction> transactions) {

        if (id != null) {
            Validator.validateId(id, "ID");
        }

        Validator.validateNonNull(renterId, "Renter ID");
        Validator.validateNonNull(carId, "Car ID");

        if (renterId != null && renterId <= 0) {
            throw new IllegalArgumentException("Renter ID must be positive");
        }
        if (carId != null && carId <= 0) {
            throw new IllegalArgumentException("Car ID must be positive");
        }

        Validator.validateDates(startTime, endTime, "Start time", "End time");
        Validator.validateNonNull(pickupLocation, "Pickup location");
        Validator.validateNonNull(transactions, "Transactions list");

        if (transactions == null) {
            transactions = new ArrayList<>();
        }

        this.id = id;
        this.renterId = renterId;
        this.carId = carId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.pickupLocation = pickupLocation;
        this.disputeDescription = disputeDescription;
        this.disputeStatus = disputeStatus;
        this.transactions = transactions;
    }

    public Booking(Long id, Long renterId, Long carId, LocalDateTime startTime, LocalDateTime endTime, TransactionStatus status, Location pickupLocation, String disputeDescription, DisputeStatus disputeStatus) {
        this(id, renterId, carId, startTime, endTime, status, pickupLocation, disputeDescription, disputeStatus, new ArrayList<>());
    }

    public List<Transaction> getTransactions() {
        return new ArrayList<>(transactions);
    }
}

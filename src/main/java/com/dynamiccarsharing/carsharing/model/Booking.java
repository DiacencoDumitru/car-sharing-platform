package com.dynamiccarsharing.carsharing.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class Booking {
    private Long id;
    private Long renterId;
    private Long carId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // will be something like: "pending", "approved", "completed"
    private String pickupLocation;
    private String disputeDescription; // for admin
    private String disputeStatus; // "open", "resolved"
}

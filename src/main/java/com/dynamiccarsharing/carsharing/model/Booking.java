package com.dynamiccarsharing.carsharing.model;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode(exclude = {"renter", "car", "pickupLocation", "transactions", "payment", "dispute"})
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private final UUID id;

    @NotNull(message = "Renter must be not null.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renter_id", nullable = false)
    private final User renter;

    @NotNull(message = "Car must be not null.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private final Car car;

    @NotNull(message = "Start time must be not null.")
    @Column(name = "start_time", nullable = false)
    private final LocalDateTime startTime;

    @NotNull(message = "End time must be not null.")
    @Column(name = "end_time", nullable = false)
    private final LocalDateTime endTime;

    @With
    @NotNull(message = "Status must be not null.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private final TransactionStatus status;

    @NotNull(message = "Pickup location must be not null.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pickup_location_id", nullable = false)
    private final Location pickupLocation;

    @With
    @Column(name = "dispute_description")
    private final String disputeDescription;

    @With
    @Column(name = "dispute_status")
    private final DisputeStatus disputeStatus;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Transaction> transactions = new ArrayList<>();

    @OneToOne(mappedBy = "booking")
    private final Payment payment;

    @OneToOne(mappedBy = "booking")
    private final Dispute dispute;

    public List<Transaction> getTransactions() {
        return List.copyOf(transactions);
    }
}

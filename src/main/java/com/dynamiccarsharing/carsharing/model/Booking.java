package com.dynamiccarsharing.carsharing.model;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
@EqualsAndHashCode(exclude = {"renter", "car", "pickupLocation", "transactions", "payment", "dispute"})
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true)
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "booking_seq")
    @SequenceGenerator(name = "booking_seq", sequenceName = "booking_seq", allocationSize = 1)
    private Long id;

    @NotNull(message = "Renter must be not null.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renter_id", nullable = false)
    private User renter;

    @NotNull(message = "Car must be not null.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @NotNull(message = "Start time must be not null.")
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @NotNull(message = "End time must be not null.")
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @With
    @NotNull(message = "Status must be not null.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @NotNull(message = "Pickup location must be not null.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pickup_location_id", nullable = false)
    private Location pickupLocation;

    @With
    @Column(name = "dispute_description")
    private String disputeDescription;

    @With
    @Enumerated(EnumType.STRING)
    @Column(name = "dispute_status")
    private DisputeStatus disputeStatus;

    @With
    @Builder.Default
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    @OneToOne(mappedBy = "booking")
    private Payment payment;

    @OneToOne(mappedBy = "booking")
    private Dispute dispute;

    public List<Transaction> getTransactions() {
        return List.copyOf(transactions);
    }
}
package com.dynamiccarsharing.booking.model;

import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString(exclude = {"transactions", "payment"})
@EqualsAndHashCode(exclude = {"transactions", "payment"})
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

    @NotNull(message = "Renter ID must be not null.")
    @Column(name = "renter_id", nullable = false)
    private Long renterId;

    @NotNull(message = "Car ID must be not null.")
    @Column(name = "car_id", nullable = false)
    private Long carId;

    @NotNull(message = "Start time must be not null.")
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @NotNull(message = "End time must be not null.")
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @NotNull(message = "Status must be not null.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @NotNull(message = "Pickup location ID must be not null.")
    @Column(name = "pickup_location_id", nullable = false)
    private Long pickupLocationId;

    @Builder.Default
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    @OneToOne(mappedBy = "booking")
    private Payment payment;

    public List<Transaction> getTransactions() {
        return List.copyOf(transactions);
    }
}
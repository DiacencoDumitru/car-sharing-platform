package com.dynamiccarsharing.carsharing.model;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.CarType;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.*;

@Getter
@ToString
@EqualsAndHashCode(exclude = {"location", "owners", "bookings", "reviews"})
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true)
@Entity
@Table(name = "cars")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private final UUID id;

    @NotNull(message = "Registration number must be not null.")
    @Column(name = "registration_number", unique = true, nullable = false)
    private final String registrationNumber;

    @NotNull(message = "Make must be not null.")
    @Column(nullable = false)
    private final String make;

    @NotNull(message = "Model must be not null.")
    @Column(nullable = false)
    private final String model;

    @With
    @NotNull(message = "Status must be not null.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private final CarStatus status;

    @NotNull(message = "Lcation must be not null.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private final Location location;

    @With
    @NotNull(message = "Price per day must be not null.")
    @Column(name = "price_per_day", nullable = false)
    private final BigDecimal price;

    @NotNull(message = "Type must be not null.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private final CarType type;

    @With
    @NotNull(message = "Verification status must be not null.")
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private final VerificationStatus verificationStatus;

    @ManyToMany(mappedBy = "cars")
    private final Set<User> owners = new HashSet<>();

    @OneToMany(mappedBy = "car")
    private final List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "car")
    private final List<CarReview> reviews = new ArrayList<>();
}
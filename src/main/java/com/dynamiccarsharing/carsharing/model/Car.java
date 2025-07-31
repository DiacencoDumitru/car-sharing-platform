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
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Entity
@Table(name = "cars")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "car_seq")
    @SequenceGenerator(name = "car_seq", sequenceName = "car_seq", allocationSize = 1)
    private Long id;

    @NotNull(message = "Registration number must be not null.")
    @Column(name = "registration_number", unique = true, nullable = false)
    private String registrationNumber;

    @NotNull(message = "Make must be not null.")
    @Column(nullable = false)
    private String make;

    @NotNull(message = "Model must be not null.")
    @Column(nullable = false)
    private String model;

    @With
    @NotNull(message = "Status must be not null.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CarStatus status;

    @With
    @NotNull(message = "Location must be not null.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @With
    @NotNull(message = "Price per day must be not null.")
    @Column(name = "price_per_day", nullable = false)
    private BigDecimal price;

    @With
    @NotNull(message = "Type must be not null.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CarType type;

    @With
    @NotNull(message = "Verification status must be not null.")
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus;

    @Builder.Default
    @ManyToMany(mappedBy = "cars")
    private Set<User> owners = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "car")
    private List<Booking> bookings = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "car")
    private List<CarReview> reviews = new ArrayList<>();

    
}
package com.dynamiccarsharing.carsharing.model;

import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.*;

@Getter
@ToString(exclude = {"cars", "bookings", "disputes"})
@EqualsAndHashCode(exclude = {"contactInfo", "cars", "bookings", "disputes", "reviewsOfUser", "reviewsByUser", "carReviewsByUser"})
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "user_seq", allocationSize = 1)
    private final Long id;

    @With
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "contact_info_id", unique = true)
    private final ContactInfo contactInfo;

    @With
    @NotNull(message = "Role must not be null.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private final UserRole role;

    @With
    @NotNull(message = "Status must not be null.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private final UserStatus status;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "user_cars",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "car_id")
    )
    private final Set<Car> cars = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "renter")
    private final List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "creationUser")
    private final List<Dispute> disputes = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private final List<UserReview> reviewsOfUser = new ArrayList<>();

    @OneToMany(mappedBy = "reviewer")
    private final List<UserReview> reviewsByUser = new ArrayList<>();

    @OneToMany(mappedBy = "reviewer")
    private final List<CarReview> carReviewsByUser = new ArrayList<>();
}
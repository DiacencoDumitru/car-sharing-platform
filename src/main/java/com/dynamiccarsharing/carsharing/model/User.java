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
    private Long id;

    @With
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "contact_info_id", unique = true)
    private ContactInfo contactInfo;

    @With
    @NotNull(message = "Role must not be null.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @With
    @NotNull(message = "Status must not be null.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "user_cars",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "car_id")
    )
    private Set<Car> cars = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "renter")
    private List<Booking> bookings = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "creationUser")
    private List<Dispute> disputes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<UserReview> reviewsOfUser = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "reviewer")
    private List<UserReview> reviewsByUser = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "reviewer")
    private List<CarReview> carReviewsByUser = new ArrayList<>();
}
package com.dynamiccarsharing.booking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Entity
@Table(name = "booking_waitlist")
public class BookingWaitlistEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "renter_id", nullable = false)
    private Long renterId;

    @NotNull
    @Column(name = "car_id", nullable = false)
    private Long carId;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @NotNull
    @Column(name = "pickup_location_id", nullable = false)
    private Long pickupLocationId;

    @Column(name = "promo_code")
    private String promoCode;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingWaitlistStatus status;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}

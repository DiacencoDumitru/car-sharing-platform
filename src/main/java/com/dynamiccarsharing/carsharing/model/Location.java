package com.dynamiccarsharing.carsharing.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
@EqualsAndHashCode(exclude = {"cars", "bookings"})
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Entity
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "location_seq")
    @SequenceGenerator(name = "location_seq", sequenceName = "location_seq", allocationSize = 1)
    private final Long id;

    @With
    @NotBlank(message = "City must be not null.")
    @Column(nullable = false)
    private final String city;

    @With
    @NotBlank(message = "State must be not null.")
    @Column(nullable = false)
    private final String state;

    @With
    @NotBlank(message = "Zip code must be not null.")
    @Column(name = "zip_code", nullable = false)
    private final String zipCode;

    @OneToMany(mappedBy = "location")
    private List<Car> cars = new ArrayList<>();

    @OneToMany(mappedBy = "pickupLocation")
    private List<Booking> bookings = new ArrayList<>();

    public List<Car> getCars() {
        return List.copyOf(cars);
    }

    public List<Booking> getBookings() {
        return List.copyOf(bookings);
    }
}
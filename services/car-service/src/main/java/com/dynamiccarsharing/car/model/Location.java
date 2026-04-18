package com.dynamiccarsharing.car.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Entity
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "location_seq")
    @SequenceGenerator(name = "location_seq", sequenceName = "location_seq", allocationSize = 1)
    private Long id;

    @NotBlank(message = "City must be not null.")
    @Column(nullable = false)
    private String city;

    @NotBlank(message = "State must be not null.")
    @Column(nullable = false)
    private String state;

    @NotBlank(message = "Zip code must be not null.")
    @Column(name = "zip_code", nullable = false)
    private String zipCode;

}
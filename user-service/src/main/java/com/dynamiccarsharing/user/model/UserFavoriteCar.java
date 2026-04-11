package com.dynamiccarsharing.user.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Entity
@Table(
        name = "user_favorite_cars",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_favorite_car", columnNames = {"user_id", "car_id"})
)
public class UserFavoriteCar {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_favorite_car_seq")
    @SequenceGenerator(name = "user_favorite_car_seq", sequenceName = "user_favorite_car_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "car_id", nullable = false)
    private Long carId;
}

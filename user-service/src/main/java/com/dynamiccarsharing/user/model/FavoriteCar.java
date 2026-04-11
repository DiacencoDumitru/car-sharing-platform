package com.dynamiccarsharing.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Entity
@Table(
        name = "favorite_cars",
        uniqueConstraints = @UniqueConstraint(name = "uk_favorite_cars_user_car", columnNames = {"user_id", "car_id"})
)
@EntityListeners(AuditingEntityListener.class)
public class FavoriteCar {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "favorite_car_seq")
    @SequenceGenerator(name = "favorite_car_seq", sequenceName = "favorite_car_seq", allocationSize = 1)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "car_id", nullable = false)
    private Long carId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}

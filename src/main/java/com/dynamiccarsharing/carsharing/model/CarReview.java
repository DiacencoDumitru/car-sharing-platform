package com.dynamiccarsharing.carsharing.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode(exclude = {"car", "reviewer"})
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true)
@Entity
@Table(name = "car_reviews")
public class CarReview {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private final UUID id;

    @NotNull(message = "Car must be not null.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private final Car car;

    @NotNull(message = "Reviewer must be not null.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private final User reviewer;

    @With
    @NotNull(message = "Comment must be not null.")
    @Column(nullable = false)
    private final String comment;
}

package com.dynamiccarsharing.car.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode()
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Entity
@Table(name = "car_reviews")
public class CarReview {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "car_review_seq")
    @SequenceGenerator(name = "car_review_seq", sequenceName = "car_review_seq", allocationSize = 1)
    private Long id;

    @NotNull(message = "Car must be not null.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @NotNull(message = "Reviewer ID must not be null.")
    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    @NotNull(message = "Comment must be not null.")
    @Column(nullable = false)
    private String comment;
}
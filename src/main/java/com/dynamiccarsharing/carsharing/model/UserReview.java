package com.dynamiccarsharing.carsharing.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@ToString
@EqualsAndHashCode(exclude = {"user", "reviewer"})
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Entity
@Table(name = "user_reviews")
public class UserReview {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_review_seq")
    @SequenceGenerator(name = "user_review_seq", sequenceName = "user_review_seq", allocationSize = 1)
    private Long id;

    @NotNull(message = "User being reviewed must not be null.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Reviewer must not be null.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @With
    @NotBlank(message = "Comment must not be blank.")
    @Column(nullable = false)
    private String comment;
}
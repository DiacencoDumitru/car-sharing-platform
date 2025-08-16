package com.dynamiccarsharing.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import com.dynamiccarsharing.contracts.enums.UserRole;
import com.dynamiccarsharing.contracts.enums.UserStatus;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString()
@EqualsAndHashCode(exclude = {"contactInfo", "reviewsOfUser", "reviewsByUser"})
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

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "contact_info_id", unique = true)
    private ContactInfo contactInfo;

    @NotNull(message = "Role must not be null.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @NotNull(message = "Status must not be null.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<UserReview> reviewsOfUser = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "reviewer")
    private List<UserReview> reviewsByUser = new ArrayList<>();
}
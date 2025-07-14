package com.dynamiccarsharing.carsharing.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode(exclude = "user")
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Entity
@Table(name = "contact_infos")
public class ContactInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private final UUID id;

    @With
    @NotBlank(message = "First name must be not null.")
    @Column(name = "first_name", nullable = false)
    private final String firstName;

    @With
    @NotBlank(message = "Last name must be not null.")
    @Column(name = "last_name", nullable = false)
    private final String lastName;

    @With
    @NotBlank(message = "Email is required and cannot be blank.")
    @Email(message = "Please provide a valid email address.")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$", message = "Invalid email format.")
    @Column(nullable = false, unique = true)
    private final String email;

    @With
    @NotBlank(message = "Phone number must be not null.")
    @Column(name = "phone_number", nullable = false)
    private final String phoneNumber;

    @OneToOne(mappedBy = "contactInfo")
    private final User user;
}

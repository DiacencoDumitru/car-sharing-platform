package com.dynamiccarsharing.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Entity
@Table(name = "contact_infos")
public class ContactInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_info_seq")
    @SequenceGenerator(name = "contact_info_seq", sequenceName = "contact_info_seq", allocationSize = 1)
    private Long id;

    @NotBlank(message = "First name must be not null.")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "Last name must be not null.")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NotBlank(message = "Email is required and cannot be blank.")
    @Email(message = "Please provide a valid email address.")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$", message = "Invalid email format.")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Phone number must be not null.")
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;
}

package com.dynamiccarsharing.user.model;

import com.dynamiccarsharing.contracts.enums.UserRole;
import com.dynamiccarsharing.contracts.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
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
public class User implements UserDetails {

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

    @Column(name = "referral_code", unique = true, length = 32)
    private String referralCode;

    @Column(name = "referred_by_user_id")
    private Long referredByUserId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return contactInfo.getPassword();
    }

    @Override
    public String getUsername() {
        return id.toString();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.BANNED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}
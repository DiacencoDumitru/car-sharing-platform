package com.dynamiccarsharing.user.model;

import com.dynamiccarsharing.contracts.enums.UserRole;
import com.dynamiccarsharing.contracts.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList; // Added import
import java.util.Collection;
import java.util.List; // Added import

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;
    private ContactInfo contactInfo;

    @BeforeEach
    void setUp() {
        contactInfo = ContactInfo.builder()
                .id(10L)
                .email("test@example.com")
                .password("password123")
                .build();

        user = User.builder()
                .id(1L)
                .contactInfo(contactInfo)
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    void testGetAuthorities() {
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertEquals(1, authorities.size());
        assertEquals("ROLE_ADMIN", authorities.iterator().next().getAuthority());
    }

    @Test
    void testGetPassword() {
        assertEquals("password123", user.getPassword());
    }

    @Test
    void testGetUsername() {
        assertEquals("1", user.getUsername());
    }

    @Test
    void testIsAccountNonExpired() {
        assertTrue(user.isAccountNonExpired());
    }

    @Test
    void testIsAccountNonLocked() {
        assertTrue(user.isAccountNonLocked());
        user.setStatus(UserStatus.BANNED);
        assertFalse(user.isAccountNonLocked());
    }

    @Test
    void testIsCredentialsNonExpired() {
        assertTrue(user.isCredentialsNonExpired());
    }

    @Test
    void testIsEnabled() {
        assertTrue(user.isEnabled());
        user.setStatus(UserStatus.SUSPENDED);
        assertFalse(user.isEnabled());
        user.setStatus(UserStatus.BANNED);
        assertFalse(user.isEnabled());
    }

    @Test
    void testBuilderAndGetters() {
        assertEquals(1L, user.getId());
        assertEquals(UserRole.ADMIN, user.getRole());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertEquals(contactInfo, user.getContactInfo());
        assertNotNull(user.getReviewsOfUser());
        assertNotNull(user.getReviewsByUser());
    }

    @Test
    void testEqualsAndHashCode() {
        User user1 = User.builder().id(1L).role(UserRole.ADMIN).build();
        User user2 = User.builder().id(1L).role(UserRole.ADMIN).build();
        User user3 = User.builder().id(2L).role(UserRole.ADMIN).build();

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1, user3);
        assertNotEquals(user1.hashCode(), user3.hashCode());
        
        user1.setContactInfo(ContactInfo.builder().id(1L).build());
        user2.setContactInfo(ContactInfo.builder().id(2L).build());
        assertEquals(user1, user2);

        user1.setReviewsOfUser(List.of(new UserReview()));
        user2.setReviewsOfUser(new ArrayList<>());
        assertEquals(user1, user2);
    }

    @Test
    void testToString() {
        String s = user.toString();
        assertTrue(s.contains("id=1"));
        assertTrue(s.contains("role=ADMIN"));
    }

    @Test
    void testAllArgsConstructor() {
        List<UserReview> reviewsOfUser = new ArrayList<>();
        List<UserReview> reviewsByUser = new ArrayList<>();
        
        User allArgsUser = new User(
                2L, 
                contactInfo, 
                UserRole.RENTER, 
                UserStatus.SUSPENDED, 
                reviewsOfUser, 
                reviewsByUser
        );

        assertEquals(2L, allArgsUser.getId());
        assertEquals(contactInfo, allArgsUser.getContactInfo());
        assertEquals(UserRole.RENTER, allArgsUser.getRole());
        assertEquals(UserStatus.SUSPENDED, allArgsUser.getStatus());
        assertEquals(reviewsOfUser, allArgsUser.getReviewsOfUser());
        assertEquals(reviewsByUser, allArgsUser.getReviewsByUser());
    }

    @Test
    void testToBuilder() {
        User copiedUser = user.toBuilder().id(10L).build();
        
        assertEquals(10L, copiedUser.getId());
        assertEquals(user.getRole(), copiedUser.getRole());
        assertEquals(user.getContactInfo(), copiedUser.getContactInfo());
    }

    @Test
    void testGetUsername_NullId() {
        User userWithoutId = User.builder().build();
        assertThrows(NullPointerException.class, userWithoutId::getUsername);
    }

    @Test
    void testGetPassword_NullContactInfo() {
        User userWithoutContactInfo = User.builder().build();
        assertThrows(NullPointerException.class, userWithoutContactInfo::getPassword);
    }
}
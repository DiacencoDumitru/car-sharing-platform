package com.dynamiccarsharing.user.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ContactInfoTest {

    private ContactInfo createContactInfo() {
        return ContactInfo.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("pass123")
                .phoneNumber("555-1234")
                .build();
    }

    @Test
    void testAllArgsConstructor() {
        ContactInfo info = new ContactInfo(1L, "Jane", "Doe", "jane@example.com", "pass456", "555-5678");

        assertEquals(1L, info.getId());
        assertEquals("Jane", info.getFirstName());
        assertEquals("Doe", info.getLastName());
        assertEquals("jane@example.com", info.getEmail());
        assertEquals("pass456", info.getPassword());
        assertEquals("555-5678", info.getPhoneNumber());
    }

    @Test
    void testBuilderAndGetters() {
        ContactInfo info = createContactInfo();
        
        assertEquals(1L, info.getId());
        assertEquals("John", info.getFirstName());
        assertEquals("Doe", info.getLastName());
        assertEquals("john.doe@example.com", info.getEmail());
        assertEquals("pass123", info.getPassword());
        assertEquals("555-1234", info.getPhoneNumber());
    }
    
    @Test
    void testSetters() {
        ContactInfo info = ContactInfo.builder().build();
        
        info.setId(2L);
        info.setFirstName("Alice");
        info.setLastName("Smith");
        info.setEmail("alice@example.com");
        info.setPassword("newPass");
        info.setPhoneNumber("111-2222");

        assertEquals(2L, info.getId());
        assertEquals("Alice", info.getFirstName());
        assertEquals("Smith", info.getLastName());
        assertEquals("alice@example.com", info.getEmail());
        assertEquals("newPass", info.getPassword());
        assertEquals("111-2222", info.getPhoneNumber());
    }

    @Test
    void testToBuilder() {
        ContactInfo info1 = createContactInfo();
        ContactInfo info2 = info1.toBuilder().email("new.email@example.com").build();
        
        assertEquals(1L, info2.getId());
        assertEquals("John", info2.getFirstName());
        assertEquals("new.email@example.com", info2.getEmail());
    }

    @Test
    void testToString() {
        ContactInfo info = createContactInfo();
        String s = info.toString();
        
        assertTrue(s.contains("id=1"));
        assertTrue(s.contains("firstName=John"));
        assertTrue(s.contains("email=john.doe@example.com"));
    }

    @Test
    void testEqualsAndHashCode_BranchCoverage() {
        ContactInfo info1 = createContactInfo();
        ContactInfo info2 = createContactInfo();
        
        assertEquals(info1, info1);
        
        assertEquals(info1, info2);
        assertEquals(info1.hashCode(), info2.hashCode());
        
        assertNotEquals(info1, null);
        
        assertNotEquals(info1, new Object());
        
        info2 = info1.toBuilder().id(2L).build();
        assertNotEquals(info1, info2);
        assertNotEquals(info1.hashCode(), info2.hashCode());

        info2 = info1.toBuilder().firstName("Jane").build();
        assertNotEquals(info1, info2);
        
        info2 = info1.toBuilder().lastName("Smith").build();
        assertNotEquals(info1, info2);
        
        info2 = info1.toBuilder().email("new@example.com").build();
        assertNotEquals(info1, info2);
        
        info2 = info1.toBuilder().password("newPass").build();
        assertNotEquals(info1, info2);
        
        info2 = info1.toBuilder().phoneNumber("000-0000").build();
        assertNotEquals(info1, info2);
    }
}
package dto.criteria;

import com.dynamiccarsharing.carsharing.dto.criteria.ContactInfoSearchCriteria;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ContactInfoSearchCriteriaTest {

    @Test
    void builder_withAllFields_setsAndGetsAllFields() {
        String phoneNumber = "+37367773888";
        String firstName = "Dumitru";
        String lastName = "Diacenco";
        String email = "dd.prodev@gmail.com";

        ContactInfoSearchCriteria criteria = ContactInfoSearchCriteria.builder()
                .phoneNumber(phoneNumber)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .build();

        assertNotNull(criteria);
        assertEquals(phoneNumber, criteria.getPhoneNumber());
        assertEquals(firstName, criteria.getFirstName());
        assertEquals(lastName, criteria.getLastName());
        assertEquals(email, criteria.getEmail());
    }
}
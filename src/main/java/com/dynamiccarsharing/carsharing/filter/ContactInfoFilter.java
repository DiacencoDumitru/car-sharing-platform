package com.dynamiccarsharing.carsharing.filter;

import com.dynamiccarsharing.carsharing.model.ContactInfo;
import lombok.Getter;

@Getter
public class ContactInfoFilter implements Filter<ContactInfo> {
    private final String email;
    private final String phoneNumber;
    private final String firstName;
    private final String lastName;

    private ContactInfoFilter(String email, String phoneNumber, String firstName, String lastName) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public static ContactInfoFilter of(String email, String phoneNumber, String firstName, String lastName) {
        return new ContactInfoFilter(email, phoneNumber, firstName, lastName);
    }

    public static ContactInfoFilter ofEmail(String email) {
        return new ContactInfoFilter(email, null, null, null);
    }

    public static ContactInfoFilter ofPhoneNumber(String phoneNumber) {
        return new ContactInfoFilter(null, phoneNumber, null, null);
    }

    public static ContactInfoFilter ofFirstName(String firstName) {
        return new ContactInfoFilter(null, null, firstName, null);
    }

    public static ContactInfoFilter ofLastName(String lastName) {
        return new ContactInfoFilter(null, null, null, lastName);
    }

    @Override
    public boolean test(ContactInfo contactInfo) {
        boolean matches = true;
        if (email != null) matches &= contactInfo.getEmail().contains(email);
        if (phoneNumber != null) matches &= contactInfo.getPhoneNumber().contains(phoneNumber);
        if (firstName != null) matches &= contactInfo.getFirstName().contains(firstName);
        if (lastName != null) matches &= contactInfo.getLastName().contains(lastName);
        return matches;
    }
}

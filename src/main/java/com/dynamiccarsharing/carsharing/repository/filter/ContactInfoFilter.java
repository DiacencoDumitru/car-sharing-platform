package com.dynamiccarsharing.carsharing.repository.filter;

import com.dynamiccarsharing.carsharing.model.ContactInfo;

public class ContactInfoFilter implements Filter<ContactInfo> {
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;

    public ContactInfoFilter setEmail(String email) {
        this.email = email;
        return this;
    }

    public ContactInfoFilter setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public ContactInfoFilter setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public ContactInfoFilter setLastName(String lastName) {
        this.lastName = lastName;
        return this;
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

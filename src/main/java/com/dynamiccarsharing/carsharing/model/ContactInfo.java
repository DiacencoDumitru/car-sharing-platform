package com.dynamiccarsharing.carsharing.model;

import com.dynamiccarsharing.carsharing.util.Validator;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class ContactInfo {
    private final Long id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phoneNumber;

    public ContactInfo(Long id, String firstName, String lastName, String email, String phoneNumber) {
        Validator.validateId(id, "ID");
        Validator.validateNonEmptyString(firstName, "First name");
        Validator.validateNonEmptyString(lastName, "Last name");
        Validator.validateEmail(email, "Email");
        Validator.validateNonEmptyString(phoneNumber, "Phone number");
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }
}

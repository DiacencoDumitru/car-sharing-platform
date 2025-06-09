package com.dynamiccarsharing.carsharing.model;

import com.dynamiccarsharing.carsharing.util.Validator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.With;

@Getter
@ToString
@EqualsAndHashCode
public class ContactInfo {
    private final Long id;
    @With
    private final String firstName;
    @With
    private final String lastName;
    @With
    private final String email;
    @With
    private final String phoneNumber;

    public ContactInfo(Long id, String firstName, String lastName, String email, String phoneNumber) {
        Validator.validateId(id, "ID");
        Validator.validateNonEmptyString(firstName, "First name");
        Validator.validateNonEmptyString(lastName, "Last name");
        Validator.validateNonEmptyString(phoneNumber, "Phone number");
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }
}

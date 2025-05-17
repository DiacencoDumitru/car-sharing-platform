package com.dynamiccarsharing.carsharing.model;

import com.dynamiccarsharing.carsharing.util.Validator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class Location {
    private final Long id;
    private final String city;
    private final String state;
    private final String zipCode;

    public Location(Long id, String city, String state, String zipCode) {
        Validator.validateId(id, "ID");
        Validator.validateNonEmptyString(city, "City");
        Validator.validateNonEmptyString(state, "State");
        Validator.validateNonEmptyString(zipCode, "Zip code");
        this.id = id;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
    }
}

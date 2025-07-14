package com.dynamiccarsharing.carsharing.repository.specification;

import com.dynamiccarsharing.carsharing.model.ContactInfo;
import org.springframework.data.jpa.domain.Specification;

public class ContactInfoSpecification {

    public static Specification<ContactInfo> firstNameContains(String firstName) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%");
    }

    public static Specification<ContactInfo> lastNameContains(String lastName) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%");
    }

    public static Specification<ContactInfo> hasEmail(String email) {
        return (root, query, cb) -> cb.equal(root.get("email"), email);
    }
}